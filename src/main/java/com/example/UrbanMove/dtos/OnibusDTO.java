package com.example.UrbanMove.dtos;

import com.example.UrbanMove.model.Localizacao;

import java.util.UUID;

public record OnibusDTO(
        UUID id,
        String linha,
        String placa,
        Localizacao localizacaoAtual
) {}