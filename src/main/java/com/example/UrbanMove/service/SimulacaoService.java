package com.example.UrbanMove.service;

import com.example.UrbanMove.dtos.OnibusDTO;
import com.example.UrbanMove.dtos.OnibusSteamDTO;
import com.example.UrbanMove.model.GtfsShape;
import com.example.UrbanMove.model.Localizacao;
import com.example.UrbanMove.model.Onibus;
import com.example.UrbanMove.repository.OnibusRepository;
import com.example.UrbanMove.repository.ShapeRepository;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

@Service
public class SimulacaoService {

    private final List<ClienteSSE> clientes = new CopyOnWriteArrayList<>();

    private final OnibusRepository onibusRepository;
    private final ShapeRepository shapeRepository;

    @Getter
    private List<Onibus> onibusEmMemoria;

    private Map<String, List<GtfsShape>> shapesCache;

    public SimulacaoService(OnibusRepository onibusRepository,
                            ShapeRepository shapeRepository) {
        this.onibusRepository = onibusRepository;
        this.shapeRepository = shapeRepository;
    }

    public List<OnibusDTO> getOnibusParaCliente(List<String> linhas) {
        List<OnibusDTO> todosOnibus = getOnibusEmMemoria().stream()
                .map(bus -> new OnibusDTO(
                        bus.getId(),
                        bus.getLocalizacaoAtual().getLatitude(),
                        bus.getLocalizacaoAtual().getLongitude(),
                        bus.getLinha(),
                        bus.getPlaca()
                ))
                .toList();

        // Se nenhuma linha foi passada, retorna todos
        if (linhas == null || linhas.isEmpty()) {
            return todosOnibus;
        }

        // Senão, filtra pelos ônibus das linhas passadas
        return todosOnibus.stream()
                .filter(bus -> linhas.contains(bus.linha()))
                .toList();
    }

    // 🔌 Conecta cliente SSE com filtro de linhas
    public SseEmitter conectar(List<String> linhas) {

        if (linhas == null) {
            linhas = Collections.emptyList(); // não faz problema, mas o getOnibusParaCliente trata vazio
        }

        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
        ClienteSSE cliente = new ClienteSSE(emitter, linhas);
        clientes.add(cliente);

        // Envia dados iniciais para o cliente
        try {
            emitter.send(getOnibusParaCliente(linhas));
        } catch (IOException e) {
            clientes.remove(cliente);
        }

        emitter.onCompletion(() -> clientes.remove(cliente));
        emitter.onTimeout(() -> clientes.remove(cliente));
        emitter.onError((e) -> clientes.remove(cliente));

        return emitter;
    }

    // 🚀 Carrega dados ao iniciar aplicação
    @PostConstruct
    public void carregarSimulacao() {

        onibusEmMemoria = onibusRepository.findAll();

        shapesCache = shapeRepository.findAll()
                .stream()
                .collect(Collectors.groupingBy(GtfsShape::getShapeId));

        shapesCache.values().forEach(lista ->
                lista.sort(Comparator.comparingInt(GtfsShape::getShapePtSequence))
        );

        for (Onibus onibus : onibusEmMemoria) {

            List<GtfsShape> pontos = shapesCache.get(onibus.getShapeId());
            if (pontos == null || pontos.isEmpty()) continue;

            GtfsShape primeiroPonto = pontos.get(0);

            Localizacao loc = new Localizacao();
            loc.setLatitude(primeiroPonto.getShapePtLat());
            loc.setLongitude(primeiroPonto.getShapePtLon());
            loc.setDataHora(LocalDateTime.now());
            loc.setOnibus(onibus);

            onibus.setLocalizacaoAtual(loc);
            onibus.setPontoAtualIndex(0);
        }

        System.out.println("🚍 Simulação carregada na memória!");
    }

    // 🔄 Atualiza posições automaticamente a cada 2 segundos
    @Scheduled(fixedRate = 2000)
    public void atualizarPosicoes() {

        if (onibusEmMemoria == null) return;

        for (Onibus onibus : onibusEmMemoria) {

            List<GtfsShape> pontos = shapesCache.get(onibus.getShapeId());
            if (pontos == null || pontos.isEmpty()) continue;

            int indexAtual = onibus.getPontoAtualIndex();
            if (indexAtual >= pontos.size() - 1) {
                indexAtual = 0;
            } else {
                indexAtual++;
            }

            GtfsShape proximoPonto = pontos.get(indexAtual);

            onibus.getLocalizacaoAtual().setLatitude(proximoPonto.getShapePtLat());
            onibus.getLocalizacaoAtual().setLongitude(proximoPonto.getShapePtLon());
            onibus.getLocalizacaoAtual().setDataHora(LocalDateTime.now());

            onibus.setPontoAtualIndex(indexAtual);
        }

        enviarAtualizacao();
    }

    // 📡 Envia atualização via SSE, respeitando filtro de linhas
    private void enviarAtualizacao() {

        // Lista para remover clientes desconectados
        List<ClienteSSE> desconectados = new ArrayList<>();

        for (ClienteSSE cliente : clientes) {

            // Para teste: enviar apenas uma linha fixa
            List<OnibusSteamDTO> payload = onibusEmMemoria.stream()
                    .filter(o -> cliente.linhasFiltro == null
                            || cliente.linhasFiltro.isEmpty()
                            || cliente.linhasFiltro.contains(o.getLinha()))
                    .map(o -> new OnibusSteamDTO(
                            o.getId(),
                            o.getLinha(),
                            o.getPlaca(),
                            o.getLocalizacaoAtual().getLatitude(),
                            o.getLocalizacaoAtual().getLongitude()
                    ))
                    .toList();

            // --- envio seguro ---
            try {
                // Se quiser testar, use apenas 1 ônibus fixo:
                // payload = List.of(new OnibusSteamDTO("1", "Teste", "ABC-1234", -22.9, -43.2));

                cliente.emitter.send(payload);
            } catch (IOException e) {
                // Cliente desconectou ou ocorreu erro na conexão
                desconectados.add(cliente);
                try {
                    cliente.emitter.complete(); // fecha SSE com segurança
                } catch (Exception ex) {
                    // ignora erros aqui
                }
            }
        }

        // Remove clientes que desconectaram, fora do loop
        clientes.removeAll(desconectados);
    }

    // 💾 Persiste o estado atual
    public void persistirEstado() {
        if (onibusEmMemoria != null) {
            onibusRepository.saveAll(onibusEmMemoria);
        }
    }

    // 🔹 Classe interna para armazenar clientes SSE com filtro de linhas
    private static class ClienteSSE {
        SseEmitter emitter;
        List<String> linhasFiltro;

        ClienteSSE(SseEmitter emitter, List<String> linhasFiltro){
            this.emitter = emitter;
            this.linhasFiltro = linhasFiltro;
        }
    }
}
