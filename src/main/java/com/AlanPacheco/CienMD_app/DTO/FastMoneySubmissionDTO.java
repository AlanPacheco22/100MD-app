package com.AlanPacheco.CienMD_app.DTO;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class FastMoneySubmissionDTO {

    @NotNull(message = "El ID del participante es obligatorio")
    private Long participantId;

    @NotNull(message = "Las respuestas son obligatorias")
    private List<String> answers;

    private long timeSpentMs;
}
