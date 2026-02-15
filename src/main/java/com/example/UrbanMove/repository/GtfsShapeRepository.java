package com.example.UrbanMove.repository;

import com.example.UrbanMove.model.GtfsShape;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface GtfsShapeRepository extends JpaRepository<GtfsShape, UUID> {
    List<GtfsShape> findByShapeIdOrderByShapePtSequenceAsc(String shapeId);
}