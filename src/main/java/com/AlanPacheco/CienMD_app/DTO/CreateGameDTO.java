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
    private int totalRounds = 3;

    @NotEmpty(message = "Debe especificar los multiplicadores por ronda")
    private int[] multipliers = {1, 1, 2};

    @Min(value = 2, message = "Minimo 2 miembros por equipo")
    @Max(value = 5, message = "Maximo 5 miembros por equipo")
    private int teamSize = 5;
}
