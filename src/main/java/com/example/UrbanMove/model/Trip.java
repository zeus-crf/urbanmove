package com.example.UrbanMove.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity(name = "gtfs_trips")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Trip {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @NotBlank
    @Column(name = "trip_id", unique = true)
    private String tripId; // trip_id do GTFS

    @NotBlank
    @Column(name = "route_id")
    private String routeId; // route_id do GTFS

    @NotBlank
    @Column(name = "shape_id")
    private String shapeId; // shape_id que define o trajeto

    @Column(name = "direction_id")
    private Integer directionId; // 0 = ida, 1 = volta (opcional)

    @Column(name = "service_id")
    private String serviceId; // opcional, vincula aos dias de operação do GTFS
}
