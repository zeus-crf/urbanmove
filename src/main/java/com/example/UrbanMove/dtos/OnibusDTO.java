package com.example.UrbanMove.dtos;

import com.example.UrbanMove.model.Localizacao;

import java.util.UUID;

public record OnibusDTO(
        UUID id,
        double latitude,
        double longitude,
        String linha,
        String placa
) {}