package com.example.UrbanMove.service;

import com.example.UrbanMove.dtos.OnibusLocalizacaoDTO;
import com.example.UrbanMove.event.OnibusAtualizadoEvent;
import com.example.UrbanMove.excecoes.OnibusNaoEncontradoException;
import com.example.UrbanMove.model.Localizacao;
import com.example.UrbanMove.model.Onibus;
import com.example.UrbanMove.repository.LocalizacaoRepository;
import com.example.UrbanMove.repository.OnibusRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class LocalizacaoService {

    private final OnibusRepository onibusRepository;
    private final LocalizacaoRepository localizacaoRepository;
    private final ApplicationEventPublisher publisher;

    public LocalizacaoService(OnibusRepository onibusRepository,
                              LocalizacaoRepository localizacaoRepository,
                              ApplicationEventPublisher publisher) {
        this.onibusRepository = onibusRepository;
        this.localizacaoRepository = localizacaoRepository;
        this.publisher = publisher;
    }


    public void gerarNovaLocalizacao() {

        List<Onibus> onibusList = onibusRepository.findAll();

        for (Onibus bus : onibusList) {

            double novaLat = gerarNovaLatitude(bus);
            double novaLng = gerarNovaLongitude(bus);

            Localizacao loc = bus.getLocalizacaoAtual();

            if (loc == null) {
                loc = new Localizacao();
            }

            loc.setLatitude(novaLat);
            loc.setLongitude(novaLng);
            loc.setDataHora(LocalDateTime.now());

            loc.setOnibus(bus);

            bus.setLocalizacaoAtual(loc);

            onibusRepository.save(bus); // salva os dois por cascade

            publisher.publishEvent(new OnibusAtualizadoEvent(bus));
        }
    }


    // Calcula variação aleatória de latitude
    private double gerarNovaLatitude(Onibus bus) {
        double atual = bus.getLocalizacaoAtual() != null ? bus.getLocalizacaoAtual().getLatitude() : -22.9068;
        return atual + gerarVariacao();
    }

    // Calcula variação aleatória de longitude
    private double gerarNovaLongitude(Onibus bus) {
        double atual = bus.getLocalizacaoAtual() != null ? bus.getLocalizacaoAtual().getLongitude() : -43.1729;
        return atual + gerarVariacao();
    }

    // Retorna variação pequena aleatória
    private double gerarVariacao() {
        return (Math.random() - 0.5) * 0.001;
    }

    // Método auxiliar para salvar localização manualmente
    private void salvar(double novaLat, double novaLng, Onibus bus) {
        Localizacao novaLoc = new Localizacao();
        novaLoc.setLatitude(novaLat);
        novaLoc.setLongitude(novaLng);
        novaLoc.setDataHora(LocalDateTime.now());
        novaLoc.setOnibus(bus);
        localizacaoRepository.save(novaLoc);
    }

    // Busca a última localização de um ônibus pelo ID
    public Localizacao buscarUltimaLocalizacaoPorOnibusId(UUID id) {
        Onibus onibus = onibusRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ônibus não encontrado"));

        Localizacao loc = localizacaoRepository.findTopByOnibusOrderByDataHoraDesc(onibus);

        if (loc == null) {
            throw new RuntimeException("Ônibus ainda não possui localização");
        }

        return loc;
    }

    // Retorna DTO com dados do ônibus + localização
    public OnibusLocalizacaoDTO buscarLocalizacaoDTO(UUID id) {
        Onibus onibus = onibusRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ônibus não encontrado"));

        Localizacao loc = localizacaoRepository.findTopByOnibusOrderByDataHoraDesc(onibus);

        if (loc == null) {
            throw new RuntimeException("Ônibus sem localização");
        }

        return new OnibusLocalizacaoDTO(
                onibus.getId(),
                onibus.getLinha(),
                onibus.getPlaca(),
                loc.getLatitude(),
                loc.getLongitude(),
                loc.getDataHora()
        );
    }
}
