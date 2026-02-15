package com.example.UrbanMove.service;

import com.example.UrbanMove.dtos.NovoOnibus;
import com.example.UrbanMove.event.OnibusAtualizadoEvent;
import com.example.UrbanMove.excecoes.OnibusNaoEncontradoException;
import com.example.UrbanMove.model.GtfsShape;
import com.example.UrbanMove.model.Localizacao;
import com.example.UrbanMove.model.Onibus;
import com.example.UrbanMove.model.Trip;
import com.example.UrbanMove.repository.LocalizacaoRepository;
import com.example.UrbanMove.repository.OnibusRepository;
import com.example.UrbanMove.repository.ShapeRepository;
import com.example.UrbanMove.repository.TripRepository;
import com.example.UrbanMove.utils.Utils;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;

import java.awt.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class OnibusService {

    private final OnibusRepository onibusRepository;
    private final LocalizacaoRepository localizacaoRepository;
    private final LocalizacaoService localizacaoService;
    private final ApplicationEventPublisher publisher; // Para eventos SSE
    private final TripRepository tripRepository;
    private final ShapeRepository shapeRepository;

    public OnibusService(OnibusRepository onibusRepository,
                         LocalizacaoRepository localizacaoRepository,
                         LocalizacaoService localizacaoService,
                         ApplicationEventPublisher publisher,
                         TripRepository tripRepository,
                         ShapeRepository shapeRepository) {
        this.onibusRepository = onibusRepository;
        this.localizacaoRepository = localizacaoRepository;
        this.localizacaoService = localizacaoService;
        this.publisher = publisher;
        this.tripRepository = tripRepository;
        this.shapeRepository = shapeRepository;
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

}
