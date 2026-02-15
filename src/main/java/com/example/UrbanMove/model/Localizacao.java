package com.example.UrbanMove.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity(name = "localizacao")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Localizacao {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @NotNull
    private Double latitude;

    @NotNull
    private Double longitude;


    private LocalDateTime dataHora;

    @ManyToOne
    @JoinColumn(name = "onibus_id", nullable = false)
    @JsonBackReference
    private Onibus onibus;

    public Localizacao(Onibus bus) {
    }
}
