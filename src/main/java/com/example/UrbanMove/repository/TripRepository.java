package com.example.UrbanMove.repository;

import com.example.UrbanMove.model.Trip;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TripRepository extends JpaRepository<Trip, Long> {
    Trip findFirstByShapeId(String shapeId);
}
