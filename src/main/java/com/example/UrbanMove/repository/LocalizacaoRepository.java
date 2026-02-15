package com.example.UrbanMove.repository;

import com.example.UrbanMove.model.Localizacao;
import com.example.UrbanMove.model.Onibus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface LocalizacaoRepository extends JpaRepository<Localizacao,Long> {
    Localizacao findTopByOnibusOrderByDataHoraDesc(Onibus bus);

    Optional<Localizacao> findTopByOnibusIdOrderByDataHoraDesc(UUID onibusId);
}
