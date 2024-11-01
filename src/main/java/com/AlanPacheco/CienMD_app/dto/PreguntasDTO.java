package com.AlanPacheco.CienMD_app.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PreguntasDTO {
    private Long id;
    private String texto;
    private List<RespuestaDTO> respuestas;

}
