package com.example.UrbanMove.dtos;

import java.time.LocalDateTime;
import java.util.UUID;

public record OnibusLocalizacaoDTO(UUID id, String linha, String placa, double latitude, double longitude, LocalDateTime dataHora) {
}
