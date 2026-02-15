package com.example.UrbanMove.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor

public class ShapePoint {
    @Id
    private UUID id; // ou combine shape_id + sequence
    private String shapeId;
    private double latitude;
    private double longitude;
    private int sequence;
}
