package com.example.UrbanMove.dtos;

import java.util.UUID;

public record OnibusSteamDTO(UUID id, String linha, String placa, double latitude, double longitude) {
}
