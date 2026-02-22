package com.example.UrbanMove;

import com.example.UrbanMove.service.OnibusService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class AtualizarRotasOnibusRunner implements CommandLineRunner {

    private final OnibusService onibusService;

    public AtualizarRotasOnibusRunner(OnibusService onibusService) {
        this.onibusService = onibusService;
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println("Iniciando atualização de rotas dos ônibus...");
        onibusService.atualizarRotasOnibus();
    }
}