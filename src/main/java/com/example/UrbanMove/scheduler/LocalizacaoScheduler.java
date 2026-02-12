package com.example.UrbanMove.scheduler;

import com.example.UrbanMove.service.LocalizacaoService;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@EnableScheduling
@Component
public class LocalizacaoScheduler {

    private final LocalizacaoService localizacaoService;

    public LocalizacaoScheduler(LocalizacaoService localizacaoService) {
        this.localizacaoService = localizacaoService;
    }

    @Scheduled(fixedRate = 1000)
    public void atualizarLoc(){
        localizacaoService.gerarNovaLocalizacao();
    }
}
