package com.example.UrbanMove.controller;

import com.example.UrbanMove.service.LocalizacaoService;
import com.example.UrbanMove.service.OnibusService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/teste")
public class TesteController {

    private final LocalizacaoService localizacaoService;
    private final OnibusService onibusService;

    public TesteController(LocalizacaoService localizacaoService, OnibusService onibusService) {
        this.localizacaoService = localizacaoService;
        this.onibusService = onibusService;
    }

    @PostMapping("/gerar-onibus")
    public String gerarOnibus() {
        onibusService.gerarOnibusAutomaticamente();
        return "Ônibus gerados com sucesso!";
    }
}
