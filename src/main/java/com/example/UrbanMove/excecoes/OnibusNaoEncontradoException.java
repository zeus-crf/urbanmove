package com.example.UrbanMove.excecoes;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.UUID;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class OnibusNaoEncontradoException extends RuntimeException {

    public OnibusNaoEncontradoException(UUID id) {
        super("Ônibus com id " + id + " não foi encontrado");
    }
}
