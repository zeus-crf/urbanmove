package com.example.UrbanMove.scheduler;

import com.example.UrbanMove.model.GtfsShape;
import com.example.UrbanMove.model.Localizacao;
import com.example.UrbanMove.model.Onibus;
import com.example.UrbanMove.repository.OnibusRepository;
import com.example.UrbanMove.repository.ShapeRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@EnableScheduling
@Component
public class LocalizacaoScheduler {

    private final OnibusRepository onibusRepository;
    private final ShapeRepository shapeRepository;

    // 🚍 Memória
    private List<Onibus> onibusEmMemoria;
    private Map<String, List<GtfsShape>> shapesCache;

    public LocalizacaoScheduler(OnibusRepository onibusRepository,
                                ShapeRepository shapeRepository) {
        this.onibusRepository = onibusRepository;
        this.shapeRepository = shapeRepository;
    }

    //  Carrega tudo uma única vez quando a aplicação inicia
    @PostConstruct
    public void inicializar() {

        // Carrega ônibus
        onibusEmMemoria = onibusRepository.findAll();

        // Carrega todos os shapes
        List<GtfsShape> todosShapes = shapeRepository.findAll();

        // Agrupa por shapeId
        shapesCache = todosShapes.stream()
                .collect(Collectors.groupingBy(GtfsShape::getShapeId));

        System.out.println("🚍 Simulação carregada na memória!");
    }

    // 🔄 Atualiza apenas em memória
    @Scheduled(fixedRate = 1000)
    public void atualizarLoc() {

        for (Onibus onibus : onibusEmMemoria) {

            List<GtfsShape> shapePoints = shapesCache.get(onibus.getShapeId());

            if (shapePoints == null || shapePoints.isEmpty()) continue;

            int pontoAtual = onibus.getPontoAtualIndex() + 1;

            if (pontoAtual >= shapePoints.size()) {
                pontoAtual = 0;
            }

            onibus.setPontoAtualIndex(pontoAtual);

            GtfsShape ponto = shapePoints.get(pontoAtual);

            Localizacao loc = onibus.getLocalizacaoAtual();

            if (loc == null) {
                loc = new Localizacao();
                onibus.setLocalizacaoAtual(loc);
            }

            loc.setLatitude(ponto.getShapePtLat());
            loc.setLongitude(ponto.getShapePtLon());
            loc.setDataHora(LocalDateTime.now());

            System.out.println("Onibus: " + onibus.getId() +
                    " | Index: " + onibus.getPontoAtualIndex() +
                    " | Lat: " + ponto.getShapePtLat());

        }


    }


    @Scheduled(fixedRate = 30000)
    public void persistirEstado() {
        onibusRepository.saveAll(onibusEmMemoria);
    }
}
