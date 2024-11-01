package com.AlanPacheco.CienMD_app.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RespuestaDTO {
    private Long id;
    private String texto;
    private int puntaje;


}