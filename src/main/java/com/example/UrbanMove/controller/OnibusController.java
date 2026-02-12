package com.example.UrbanMove.controller;

import com.example.UrbanMove.dtos.NovoOnibus;
import com.example.UrbanMove.event.OnibusAtualizadoEvent;
import com.example.UrbanMove.model.Onibus;
import com.example.UrbanMove.service.LocalizacaoService;
import com.example.UrbanMove.service.OnibusService;
import com.example.UrbanMove.utils.Utils;
import org.springframework.context.event.EventListener;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

@RestController
@RequestMapping("/onibus")
public class OnibusController {

    private final LocalizacaoService localizacaoService;
    private final OnibusService onibusService;

    // Lista thread-safe para armazenar todos os SSE emitters ativos
    private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();

    public OnibusController(LocalizacaoService localizacaoService, OnibusService onibusService) {
        this.localizacaoService = localizacaoService;
        this.onibusService = onibusService;
    }

    // --- Endpoints REST normais ---
    @GetMapping("/{id}/localizacao")
    public ResponseEntity<?> localizacaoAtual(@PathVariable UUID id) {
        return ResponseEntity.ok(localizacaoService.buscarLocalizacaoDTO(id));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Onibus> umOnibus(@PathVariable UUID id) {
        return ResponseEntity.ok(onibusService.umOnibus(id));
    }

    @GetMapping
    public ResponseEntity<List<Onibus>> listaDeOnibus() {
        return ResponseEntity.ok(onibusService.listaDeOnibusComLocalizacaoAtual());
    }

    @PostMapping
    public ResponseEntity<Onibus> criarOnibus(@RequestBody NovoOnibus novoOnibus) {
        Onibus onibus = new Onibus();
        Utils.copyNonNullProperties(novoOnibus, onibus);
        return ResponseEntity.ok(onibusService.criarOnibus(onibus));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> editarOnibus(@RequestBody NovoOnibus novoOnibus, @PathVariable UUID id) {
        return onibusService.editarOnibus(id, novoOnibus);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletarOnibus(@PathVariable UUID id) {
        return onibusService.deletarOnibus(id);
    }

    // --- SSE para enviar atualizações em tempo real ---
    @GetMapping("/stream")
    public SseEmitter streamOnibus() {
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE); // tempo ilimitado
        emitters.add(emitter);

        // Remove emitter se completar ou expirar
        emitter.onCompletion(() -> emitters.remove(emitter));
        emitter.onTimeout(() -> emitters.remove(emitter));

        return emitter;
    }

    // Evento do Spring para atualizar todos os clientes SSE quando um ônibus mudar
    @EventListener
    public void enviarAtualizacao(OnibusAtualizadoEvent event) {
        Onibus bus = event.getOnibus();
        emitters.forEach(emitter -> {
            try {
                emitter.send(bus);
            } catch (IOException e) {
                emitters.remove(emitter);
            }
        });
    }

}
