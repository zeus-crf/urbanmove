package com.example.UrbanMove.service;

import com.example.UrbanMove.dtos.NovoOnibus;
import com.example.UrbanMove.event.OnibusAtualizadoEvent;
import com.example.UrbanMove.excecoes.OnibusNaoEncontradoException;
import com.example.UrbanMove.model.Localizacao;
import com.example.UrbanMove.model.Onibus;
import com.example.UrbanMove.repository.LocalizacaoRepository;
import com.example.UrbanMove.repository.OnibusRepository;
import com.example.UrbanMove.utils.Utils;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class OnibusService {

    private final OnibusRepository onibusRepository;
    private final LocalizacaoRepository localizacaoRepository;
    private final LocalizacaoService localizacaoService;
    private final ApplicationEventPublisher publisher; // Para eventos SSE

    public OnibusService(OnibusRepository onibusRepository,
                         LocalizacaoRepository localizacaoRepository,
                         LocalizacaoService localizacaoService,
                         ApplicationEventPublisher publisher) {
        this.onibusRepository = onibusRepository;
        this.localizacaoRepository = localizacaoRepository;
        this.localizacaoService = localizacaoService;
        this.publisher = publisher;
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


    public ResponseEntity<?> deletarOnibus(UUID id) {
        Onibus onibus = buscarOnibus(id);
        onibusRepository.delete(onibus);
        return ResponseEntity.ok().body("Deletado");
    }

    // Lista todos os ônibus com a localização atual
    public List<Onibus> listaDeOnibusComLocalizacaoAtual() {
        List<Onibus> onibusList = onibusRepository.findAll();
        for (Onibus bus : onibusList) {
            Localizacao ultima = localizacaoService.buscarUltimaLocalizacaoPorOnibusId(bus.getId());
            bus.setLocalizacaoAtual(ultima);
        }
        return onibusList;
    }

    // Atualiza a localização de todos os ônibus e dispara evento para SSE
    public void gerarNovaLocalizacao() {
        List<Onibus> onibusList = onibusRepository.findAll();

        for (Onibus bus : onibusList) {
            double novaLat = gerarNovaLatitude();
            double novaLng = gerarNovaLongitude();

            Localizacao novaLoc = new Localizacao();
            novaLoc.setLatitude(novaLat);
            novaLoc.setLongitude(novaLng);
            novaLoc.setDataHora(LocalDateTime.now());

            // Salva no banco apenas a última localização
            bus.setLocalizacaoAtual(novaLoc);
            onibusRepository.save(bus);

            // Dispara evento para o SSE
            publisher.publishEvent(new OnibusAtualizadoEvent(bus));
        }
    }

    private double gerarNovaLatitude() {
        return -22.9068 + (Math.random() - 0.5) * 0.01;
    }

    private double gerarNovaLongitude() {
        return -43.1729 + (Math.random() - 0.5) * 0.01;
    }

}
