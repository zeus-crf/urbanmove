package com.example.UrbanMove.repository;

import com.example.UrbanMove.model.GtfsShape;
import com.example.UrbanMove.model.ShapePoint;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;


public interface ShapeRepository extends JpaRepository<GtfsShape, Integer> {

    List<GtfsShape> findByShapeIdOrderByShapePtSequenceAsc(String shapeId);

}


