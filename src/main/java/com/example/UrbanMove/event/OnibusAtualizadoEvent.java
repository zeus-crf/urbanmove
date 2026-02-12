package com.example.UrbanMove.event;

import com.example.UrbanMove.model.Onibus;
import org.springframework.context.ApplicationEvent;

public class OnibusAtualizadoEvent extends ApplicationEvent {

    private final Onibus onibus;

    public OnibusAtualizadoEvent(Onibus onibus) {
        super(onibus); // chama o construtor da superclasse
        this.onibus = onibus;
    }

    public Onibus getOnibus() {
        return onibus;
    }
}
