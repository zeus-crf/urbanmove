package com.example.UrbanMove.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity(name = "rota")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Rota {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String nome;

    private double latInicio;
    private double lngInicio;

    private double latFim;
    private double lngFim;



}
