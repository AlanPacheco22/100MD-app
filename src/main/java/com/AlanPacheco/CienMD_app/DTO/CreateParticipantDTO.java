package com.AlanPacheco.CienMD_app.DTO;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateParticipantDTO {

    @NotBlank(message = "El nombre es obligatorio")
    private String name;

    @Min(value = 1, message = "El equipo debe ser 1 o 2")
    @Max(value = 2, message = "El equipo debe ser 1 o 2")
    private int team = 1;
}
