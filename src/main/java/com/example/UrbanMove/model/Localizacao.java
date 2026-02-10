package com.example.UrbanMove.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
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

    @NotBlank
    private Double latitude;

    @NotBlank
    private Double longitude;

    @CreationTimestamp
    private LocalDateTime dataHora;

    @ManyToOne
    @Column(name = "onibus_id")
    private Onibus onibus;
}
