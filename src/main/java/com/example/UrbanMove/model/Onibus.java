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


    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "localizacao_id")
    private Localizacao localizacaoAtual;

}
