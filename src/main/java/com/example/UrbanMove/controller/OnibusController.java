package com.example.UrbanMove.controller;

import com.example.UrbanMove.dtos.BusDTO;
import com.example.UrbanMove.dtos.NovoOnibus;
import com.example.UrbanMove.dtos.OnibusDTO;
import com.example.UrbanMove.event.OnibusAtualizadoEvent;
import com.example.UrbanMove.model.GtfsRoutes;
import com.example.UrbanMove.model.Onibus;
import com.example.UrbanMove.service.LocalizacaoService;
import com.example.UrbanMove.service.OnibusService;
import com.example.UrbanMove.service.SimulacaoService;
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
@CrossOrigin(
        origins = "http://localhost:5173", // frontend
        allowCredentials = "true"
)
public class OnibusController {

    private final LocalizacaoService localizacaoService;
    private final OnibusService onibusService;
    private final SimulacaoService simulacaoService;


    public OnibusController(LocalizacaoService localizacaoService,
                            OnibusService onibusService,
                            SimulacaoService simulacaoService) {
        this.localizacaoService = localizacaoService;
        this.onibusService = onibusService;
        this.simulacaoService = simulacaoService;
    }

    @GetMapping("/routes")
    public ResponseEntity<List<BusDTO>> getBuses(
            @RequestParam(required = false) String start,
            @RequestParam(required = false) String end) {

        List<BusDTO> buses = onibusService.findBusesBetween(start, end);
        return ResponseEntity.ok(buses);
    }



    @GetMapping("/routes/{id}")
    public GtfsRoutes getRouteById(@PathVariable Long id){
        return onibusService.getRouteById(id);
    }


    @GetMapping("/{id}/localizacao")
    public ResponseEntity<?> localizacaoAtual(@PathVariable UUID id) {
        return ResponseEntity.ok(localizacaoService.buscarLocalizacaoDTO(id));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Onibus> umOnibus(@PathVariable UUID id) {
        return ResponseEntity.ok(onibusService.umOnibus(id));
    }

    @GetMapping
    public ResponseEntity<List<OnibusDTO>> listaDeOnibus() {
        return ResponseEntity.ok(onibusService.lista());
    }

    @GetMapping("/stream")
    public SseEmitter stream(@RequestParam(required = false) List<String> linhas){
        return simulacaoService.conectar(linhas);
    }

    @GetMapping("/linhas")
    public ResponseEntity<List<String>> todasAsLinhas(){
        return onibusService.todasAsLinhas();
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
}
