package com.example.UrbanMove.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity(name = "gtfs_shapes")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GtfsShape {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;  // só para a chave primária do banco, não confundir com shape_id

    private String shapeId;       // corresponde ao shape_id do GTFS
    private double shapePtLat;    // latitude
    private double shapePtLon;    // longitude
    private int shapePtSequence;  // sequência do ponto
    private Double shapeDistTraveled; // opcional
}