package com.example.UrbanMove.repository;

import com.example.UrbanMove.model.GtfsRoutes;
import com.example.UrbanMove.model.GtfsShape;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface GtfsRoutesRepository extends JpaRepository<GtfsRoutes, Long> {
    @Query("SELECT r FROM GtfsRoutes r WHERE LOWER(r.route_long_name) LIKE LOWER(CONCAT('%', :search, '%'))")
    List<GtfsRoutes> searchRoutes(@Param("search") String search);


    GtfsRoutes findByShapes_ShapeId(String shapeId);

    @Query("SELECT g FROM GtfsRoutes g WHERE g.routeId = :routeId")
    GtfsRoutes findByRouteId(@Param("routeId") String routeId);

    @Query("""
        SELECT r.route_long_name
        FROM GtfsRoutes r
        WHERE LOWER(r.route_long_name) LIKE LOWER(CONCAT(:query, '%'))
        ORDER BY r.route_long_name ASC
        """)
    List<String> findNames(@Param("query") String query);
}
