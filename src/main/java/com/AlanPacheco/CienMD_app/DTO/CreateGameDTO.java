package com.AlanPacheco.CienMD_app.DTO;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateGameDTO {

    @Min(value = 2, message = "Minimo 2 rondas")
    @Max(value = 10, message = "Maximo 10 rondas")
    private int totalRounds = 5;

    @NotEmpty(message = "Debe especificar los multiplicadores por ronda")
    private int[] multipliers = {1, 1, 2, 2, 3};

    @Min(value = 2, message = "Minimo 2 miembros por equipo")
    @Max(value = 5, message = "Maximo 5 miembros por equipo")
    private int teamSize = 5;

    @Min(value = 100, message = "Minimo 100 puntos para ganar")
    @Max(value = 9999, message = "Maximo 9999 puntos para ganar")
    private int targetScore = 300;

    private boolean timerEnabled = true;

    @Min(value = 5, message = "Minimo 5 segundos por turno")
    @Max(value = 60, message = "Maximo 60 segundos por turno")
    private int turnTimeLimit = 10;
}
