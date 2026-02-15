package com.example.UrbanMove;

import com.example.UrbanMove.service.OnibusService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class UrbanMoveApplication {

    public static void main(String[] args) {
        SpringApplication.run(UrbanMoveApplication.class, args);
    }

    @Bean
    CommandLineRunner run(OnibusService onibusService) {
        return args -> {
            onibusService.gerarOnibusAutomaticamente();
        };
    }
}