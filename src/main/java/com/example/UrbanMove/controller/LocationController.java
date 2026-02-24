package com.example.UrbanMove.controller;

import com.example.UrbanMove.service.OnibusService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/locations")
@CrossOrigin(
        origins = "http://localhost:5173", // frontend
        allowCredentials = "true"
)
public class LocationController {

    private final OnibusService onibusService;

    public LocationController(OnibusService onibusService) {
        this.onibusService = onibusService;
    }

    @GetMapping
    public ResponseEntity<List<String>> search(@RequestParam String query) {
        return ResponseEntity.ok(onibusService.searchLocations(query));
    }
}
