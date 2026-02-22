package com.example.UrbanMove.service;

import com.example.UrbanMove.dtos.BusDTO;
import com.example.UrbanMove.dtos.NovoOnibus;
import com.example.UrbanMove.dtos.OnibusDTO;
import com.example.UrbanMove.event.OnibusAtualizadoEvent;
import com.example.UrbanMove.excecoes.OnibusNaoEncontradoException;
import com.example.UrbanMove.model.*;
import com.example.UrbanMove.repository.*;

import com.example.UrbanMove.utils.Utils;
import jakarta.annotation.PostConstruct;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.awt.*;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

@Service
public class OnibusService {

    private final OnibusRepository onibusRepository;
    private final LocalizacaoRepository localizacaoRepository;
    private final LocalizacaoService localizacaoService;
    private final ApplicationEventPublisher publisher; // Para eventos SSE
    private final TripRepository tripRepository;
    private final ShapeRepository shapeRepository;
    private final SimulacaoService simulacaoService;
    private final GtfsRoutesRepository gtfsRoutesRepository;

    private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();

    public OnibusService(OnibusRepository onibusRepository,
                         LocalizacaoRepository localizacaoRepository,
                         LocalizacaoService localizacaoService,
                         ApplicationEventPublisher publisher,
                         TripRepository tripRepository,
                         ShapeRepository shapeRepository,
                         SimulacaoService simulacaoService, GtfsRoutesRepository gtfsRoutesRepository) {
        this.onibusRepository = onibusRepository;
        this.localizacaoRepository = localizacaoRepository;
        this.localizacaoService = localizacaoService;
        this.publisher = publisher;
        this.tripRepository = tripRepository;
        this.shapeRepository = shapeRepository;

        this.simulacaoService = simulacaoService;
        this.gtfsRoutesRepository = gtfsRoutesRepository;
    }

    public List<GtfsRoutes> searchRoutes(@RequestParam(required = false, defaultValue = "") String search) {
        return gtfsRoutesRepository.searchRoutes(search);
    }

    public GtfsRoutes getRouteById(@PathVariable Long id){
        return gtfsRoutesRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("ID" + id + " nao encontrado"));
    }


    public List<Onibus> listaDeOnibus() {
        return onibusRepository.findAll();
    }

    public Onibus buscarOnibus(UUID id) {
        return onibusRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ônibus não encontrado: " + id));
    }

    public Onibus umOnibus(@PathVariable UUID id){
        return onibusRepository.findById(id).orElseThrow(() -> new OnibusNaoEncontradoException(id));
    }

    public Onibus criarOnibus(Onibus onibus) {
        return onibusRepository.save(onibus);
    }

    public ResponseEntity<Onibus> editarOnibus(UUID id, NovoOnibus dados) {

        Onibus existente = onibusRepository.findById(id)
                .orElseThrow(() -> new OnibusNaoEncontradoException(id));

        Utils.copyNonNullProperties(dados, existente);

        Onibus salvo = onibusRepository.save(existente);

        return ResponseEntity.ok(salvo);
    }

    public List<BusDTO> findBusesBetween(String start, String end) {

        // 1️⃣ Se não passar start e end, retorna todos os ônibus
        if ((start == null || start.isBlank()) && (end == null || end.isBlank())) {
            return onibusRepository.findAll().stream()
                    .map(bus -> new BusDTO(
                            bus.getId().toString(),
                            bus.getRoute() != null ? bus.getRoute().getRouteId() : null,
                            bus.getLocalizacaoAtual().getLatitude(),
                            bus.getLocalizacaoAtual().getLongitude()
                    ))
                    .toList();
        }

        // 2️⃣ Normaliza inputs
        String s = start != null ? start.toLowerCase().trim() : "";
        String e = end != null ? end.toLowerCase().trim() : "";

        System.out.println("======================================");
        System.out.println("BUSCANDO ENTRE: " + start + " | " + end);

        // 3️⃣ Filtra rotas pelo nome
        List<GtfsRoutes> rotasEncontradas = gtfsRoutesRepository.findAll().stream()
                .filter(route -> {
                    String name = route.getRoute_long_name().toLowerCase();
                    return name.contains(s) && name.contains(e);
                })
                .toList();

        System.out.println("ROTAS ENCONTRADAS: " + rotasEncontradas.size());

        // 4️⃣ Filtra ônibus que estão nessas rotas
        List<BusDTO> resultado = onibusRepository.findAll().stream()
                .filter(bus -> bus.getRoute() != null
                        && rotasEncontradas.stream()
                        .anyMatch(r -> r.getRouteId().equals(bus.getRoute().getRouteId()))
                )
                .map(bus -> new BusDTO(
                        bus.getId().toString(),
                        bus.getRoute().getRouteId(),
                        bus.getLocalizacaoAtual().getLatitude(),
                        bus.getLocalizacaoAtual().getLongitude()
                ))
                .toList();

        System.out.println("TOTAL DE ÔNIBUS ENCONTRADOS: " + resultado.size());
        System.out.println("======================================");

        return resultado;
    }

    public ResponseEntity<?> deletarOnibus(UUID id) {
        Onibus onibus = buscarOnibus(id);
        onibusRepository.delete(onibus);
        return ResponseEntity.ok().body("Deletado");
    }


    // Lista todos os ônibus com a localização atual
    public List<OnibusDTO> lista() {
        return simulacaoService.getOnibusEmMemoria()
                .stream()
                .map(bus -> new OnibusDTO(
                        bus.getId(),
                        bus.getLocalizacaoAtual().getLatitude(),
                        bus.getLocalizacaoAtual().getLongitude(),
                        bus.getLinha(),
                        bus.getPlaca()
                ))
                .toList();
    }

    private void enviarAtualizacao() {

        List<OnibusDTO> dados = lista();

        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(dados);
            } catch (IOException e) {
                emitters.remove(emitter);
                System.out.println("Cliente desconectou: " + e.getMessage());
            }
        }
    }


    // Atualiza a localização de todos os ônibus e dispara evento para SSE
    public void gerarNovaLocalizacao() {
        List<Onibus> onibusList = onibusRepository.findAll();

        for (Onibus bus : onibusList) {

         moverOnibusPeloShape(bus);
         publisher.publishEvent(new OnibusAtualizadoEvent(bus));
        }
    }

    public void moverOnibusPeloShape(Onibus onibus){
        List<GtfsShape> pontos = shapeRepository.findByShapeIdOrderByShapePtSequenceAsc(onibus.getShapeId());

        if (pontos.isEmpty()) return;

        int index = onibus.getPontoAtualIndex();

        if (index >= pontos.size()){
            index = 0;
        }

        GtfsShape pontoAtual = pontos.get(index);

        Localizacao loc = new Localizacao();

        loc.setLatitude(pontoAtual.getShapePtLat());
        loc.setLongitude(pontoAtual.getShapePtLon());
        loc.setDataHora(LocalDateTime.now());

        onibus.setLocalizacaoAtual(loc);
        onibus.setPontoAtualIndex(index + 1);

        onibusRepository.save(onibus);
    }

    public void gerarOnibusAutomaticamente() {
        // Pega todas as trips do banco
        List<Trip> trips = tripRepository.findAll();

        Set<String> shapeIdsExistentes = onibusRepository.findAll()
                .stream()
                .map(Onibus::getShapeId)
                .collect(Collectors.toSet());

        for (Trip trip : trips) {

            if (shapeIdsExistentes.contains(trip.getShapeId())){
                continue;
            }

            // Cria novo ônibus
            Onibus onibus = new Onibus();
            onibus.setLinha(trip.getRouteId());      // linha da viagem
            onibus.setPlaca(String.valueOf(trip.getId())); // placa única para teste
            onibus.setShapeId(trip.getShapeId());     // shape da trip
            onibus.setPontoAtualIndex(0);
            onibus.setIndo(true);                     // sentido inicial
            onibusRepository.save(onibus);

            System.out.println("Ônibus gerado: " + onibus.getPlaca() + " | Shape: " + onibus.getShapeId());
        }
    }

    @PostConstruct
    public void atualizarRotasOnibusExistentes() {
        List<Onibus> onibusList = onibusRepository.findAll();

        for (Onibus onibus : onibusList) {
            if (onibus.getRoute() == null && onibus.getShapeId() != null) {
                // Busca a rota pelo shapeId
                GtfsRoutes rota = gtfsRoutesRepository.findByShapes_ShapeId(onibus.getShapeId());
                if (rota != null) {
                    onibus.setRoute(rota);
                    onibusRepository.save(onibus);
                    System.out.println("Ônibus " + onibus.getPlaca() + " atualizado com rota: " + rota.getRoute_long_name());
                } else {
                    System.out.println("Ônibus " + onibus.getPlaca() + " sem rota para shape: " + onibus.getShapeId());
                }
            }
        }
    }

    @Transactional
    public void atualizarRotasOnibus() {
        List<Onibus> onibusList = onibusRepository.findAll();

        for (Onibus onibus : onibusList) {
            if (onibus.getRoute() == null && onibus.getShapeId() != null) {

                // Procura a primeira trip que usa o mesmo shape_id
                Trip trip = tripRepository.findFirstByShapeId(onibus.getShapeId());

                if (trip != null) {
                    // Busca a rota usando o route_id da trip
                    GtfsRoutes rota = gtfsRoutesRepository.findByRouteId(trip.getRouteId());

                    if (rota != null) {
                        onibus.setRoute(rota);
                        onibusRepository.save(onibus);
                        System.out.println("Ônibus " + onibus.getPlaca() + " atualizado com rota: " + rota.getRoute_long_name());
                    } else {
                        System.out.println("Ônibus " + onibus.getPlaca() + " sem rota para route_id da trip: " + trip.getRouteId());
                    }

                } else {
                    System.out.println("Ônibus " + onibus.getPlaca() + " sem trip para shape: " + onibus.getShapeId());
                }
            }
        }
        System.out.println("Atualização de rotas dos ônibus concluída!");
    }

    public ResponseEntity<List<String>> todasAsLinhas(){
        List<String> linhas = simulacaoService.getOnibusEmMemoria()
                .stream()
                .map(Onibus::getLinha)
                .distinct()
                .sorted()
                .toList();
        return ResponseEntity.ok(linhas);
    }

}
