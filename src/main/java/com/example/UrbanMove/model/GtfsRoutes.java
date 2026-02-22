package com.example.UrbanMove.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "gtfs_routes")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class GtfsRoutes {
    @Id
    private String id;

    private String routeId;

    private Long agency_id;

    @Column(name = "route_long_name")
    private String route_long_name;

    private String route_desc;

    private Long route_type;

    private String route_color;

    private String route_text_color;

    private String route_url;

    @OneToMany(mappedBy = "route", cascade = CascadeType.ALL)
    private List<GtfsShape> shapes;

}
