package com.AlanPacheco.CienMD_app.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BuzzInDTO {

    @NotNull(message = "El ID del participante es obligatorio")
    private Long participantId;

    @NotBlank(message = "La respuesta es obligatoria")
    private String answerText;
}
