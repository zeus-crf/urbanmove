package com.example.UrbanMove.repository;

import com.example.UrbanMove.model.ShapePoint;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ShapeRepository extends JpaRepository<ShapePoint, UUID> {
    List<ShapePoint> findByShapeIdOrderBySequenceAsc(String shapeId);
}

