package com.example.UrbanMove.service;

import com.example.UrbanMove.dtos.OnibusLocalizacaoDTO;
import com.example.UrbanMove.event.OnibusAtualizadoEvent;
import com.example.UrbanMove.excecoes.OnibusNaoEncontradoException;
import com.example.UrbanMove.model.*;
import com.example.UrbanMove.repository.GtfsShapeRepository;
import com.example.UrbanMove.repository.LocalizacaoRepository;
import com.example.UrbanMove.repository.OnibusRepository;
import com.example.UrbanMove.repository.ShapeRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service

public class LocalizacaoService {

    private final OnibusRepository onibusRepository;
    private final LocalizacaoRepository localizacaoRepository;
    private final ShapeRepository shapeRepository;
    private final GtfsShapeRepository gtfsShapeRepository;

    public LocalizacaoService(OnibusRepository onibusRepository,
                              LocalizacaoRepository localizacaoRepository,
                              ShapeRepository shapeRepository,
                              GtfsShapeRepository gtfsShapeRepository) {
        this.onibusRepository = onibusRepository;
        this.localizacaoRepository = localizacaoRepository;
        this.shapeRepository = shapeRepository;
        this.gtfsShapeRepository = gtfsShapeRepository;
    }

    @Scheduled(fixedRate = 1000) // atualiza a cada 1 segundo
    public void atualizarLoc() {
        gerarNovaLocalizacao();
    }


    public void gerarNovaLocalizacao() {
        // Busca todos os ônibus ativos
        List<Onibus> onibusList = onibusRepository.findAll();

        for (Onibus onibus : onibusList) {
            String shapeId = onibus.getShapeId();

            if (shapeId == null) {
                System.out.println("Ônibus sem shapeId: " + onibus.getId());
                continue;
            }

            // Busca os pontos do shape no GTFS
            List<GtfsShape> shapePoints = gtfsShapeRepository.findByShapeIdOrderByShapePtSequenceAsc(shapeId);
            if (shapePoints.isEmpty()) {
                System.out.println("Shape vazio para o ônibus: " + onibus.getId());
                continue;
            }

            // Atualiza o ponto atual do ônibus
            int pontoAtual = onibus.getPontoAtualIndex() + 1;
            if (pontoAtual >= shapePoints.size()) {
                pontoAtual = 0; // reinicia ou inverte direção
            }
            onibus.setPontoAtualIndex(pontoAtual);

            // Pega o ponto atual do shape GTFS
            GtfsShape ponto = shapePoints.get(pontoAtual);

            // Atualiza a localização existente ou cria se não tiver
            Localizacao locAtual = onibus.getLocalizacaoAtual();
            if (locAtual == null) {
                locAtual = new Localizacao();
                locAtual.setOnibus(onibus);
            }

            locAtual.setLatitude(ponto.getShapePtLat());
            locAtual.setLongitude(ponto.getShapePtLon());
            locAtual.setDataHora(LocalDateTime.now());

            // Salva a localização e atualiza referência no ônibus
            localizacaoRepository.save(locAtual);
            onibus.setLocalizacaoAtual(locAtual);
            onibusRepository.save(onibus);
        }
    }




    // Busca a última localização de um ônibus pelo ID
    public Localizacao buscarUltimaLocalizacaoPorOnibusId(UUID onibusId) {
        Optional<Localizacao> ultima = localizacaoRepository
                .findTopByOnibusIdOrderByDataHoraDesc(onibusId);

        return ultima.orElse(null); // retorna null se não existir localização
    }


    // Retorna DTO com dados do ônibus + localização
    public OnibusLocalizacaoDTO buscarLocalizacaoDTO(UUID id) {
        Onibus onibus = onibusRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ônibus não encontrado"));

        Localizacao loc = localizacaoRepository.findTopByOnibusOrderByDataHoraDesc(onibus);

        if (loc == null) {
            throw new RuntimeException("Ônibus sem localização");
        }

        return new OnibusLocalizacaoDTO(
                onibus.getId(),
                onibus.getLinha(),
                onibus.getPlaca(),
                loc.getLatitude(),
                loc.getLongitude(),
                loc.getDataHora()
        );
    }
}
