package com.example.UrbanMove.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Entity(name = "onibus")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Onibus {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotBlank
    private String linha;

    @NotBlank
    private String placa;

    // Localização atual do ônibus
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "localizacao_id")
    private Localizacao localizacaoAtual;


    @Column(name = "shape_id")
    private String shapeId; // muda de String para UUID


    private int pontoAtualIndex;

    // Direção do ônibus (ida ou volta)
    private boolean indo; // true = indo, false = voltando

    @ManyToOne
    @JoinColumn(name = "route_id") // FK no banco
    private GtfsRoutes route;
}
