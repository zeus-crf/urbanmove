package com.example.UrbanMove.service;

import com.example.UrbanMove.model.GtfsShape;
import com.example.UrbanMove.model.Onibus;
import com.example.UrbanMove.repository.OnibusRepository;
import com.example.UrbanMove.repository.ShapeRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SimulacaoService {
    private final OnibusRepository onibusRepository;
    private final ShapeRepository shapeRepository;

    private List<Onibus> onibusEmMemoria;
    private Map<String, List<GtfsShape>> shapesCache;


    public SimulacaoService(OnibusRepository onibusRepository, ShapeRepository shapeRepository) {
        this.onibusRepository = onibusRepository;
        this.shapeRepository = shapeRepository;
    }


}
