package com.example.UrbanMove.repository;

import com.example.UrbanMove.model.Onibus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface OnibusRepository extends JpaRepository<Onibus, UUID> {

}
