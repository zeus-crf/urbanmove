package com.example.UrbanMove.service;

import com.example.UrbanMove.model.Localizacao;
import com.example.UrbanMove.model.Onibus;
import com.example.UrbanMove.repository.LocalizacaoRepository;
import com.example.UrbanMove.repository.OnibusRepository;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

import static java.lang.Math.random;


@Service
public class LocalizacaoService {

    private final OnibusRepository onibusRepository;
    private final LocalizacaoRepository localizacaoRepository;

    public LocalizacaoService(OnibusRepository onibusRepository,
                              LocalizacaoRepository localizacaoRepository) {
        this.onibusRepository = onibusRepository;
        this.localizacaoRepository = localizacaoRepository;
    }

    public void gerarNovaLocalizacao() {
        List<Onibus> onibusList = onibusRepository.findAll();

        for (Onibus bus : onibusList) {

            Localizacao ultima =
                    localizacaoRepository.findTopByOnibusOrderByDataHoraDesc(bus);

            double novaLat;
            double novaLng;

            if (ultima == null) {
                novaLat = -22.9068;
                novaLng = -43.1729;
            } else {
                novaLat = ultima.getLatitude() + gerarVariacao();
                novaLng = ultima.getLongitude() + gerarVariacao();
            }

            salvar(novaLat, novaLng, bus);
        }
    }

    private double gerarVariacao() {
        return (Math.random() - 0.5) * 0.001;
    }

    private void salvar(double novaLat, double novaLng, Onibus bus) {
        Localizacao novaLoc = new Localizacao();
        novaLoc.setLatitude(novaLat);
        novaLoc.setLongitude(novaLng);
        novaLoc.setDataHora(LocalDateTime.now());
        novaLoc.setOnibus(bus);
        localizacaoRepository.save(novaLoc);
    }
}

