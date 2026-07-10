package com.AlanPacheco.CienMD_app.DTO;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RoundDTO {

    @NotNull(message = "El participante es obligatorio")
    private Long participantId;

    @NotNull(message = "La pregunta es obligatoria")
    private Long gameQuestionId;

    @NotBlank(message = "La respuesta no puede estar vacía")
    private String answerText;

    @Min(value = 1, message = "El multiplicador debe ser al menos 1")
    private int roundMultiplier = 1;
}
