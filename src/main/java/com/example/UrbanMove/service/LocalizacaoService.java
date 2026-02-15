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
