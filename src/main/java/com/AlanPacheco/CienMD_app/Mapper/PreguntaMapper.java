package com.AlanPacheco.CienMD_app.Mapper;

import com.AlanPacheco.CienMD_app.dto.PreguntasDTO;
import com.AlanPacheco.CienMD_app.Model.Preguntas;
import com.AlanPacheco.CienMD_app.dto.RespuestaDTO;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class PreguntaMapper {

    public PreguntasDTO toDTO(Preguntas pregunta) {
        PreguntasDTO dto = new PreguntasDTO();
        dto.setId(pregunta.getId());
        dto.setTexto(pregunta.getTexto());
        dto.setRespuestas(pregunta.getRespuestas().stream().map(respuesta -> {
            RespuestaDTO respuestaDTO = new RespuestaDTO();
            respuestaDTO.setId(respuesta.getId());
            respuestaDTO.setTexto(respuesta.getTexto());
            respuestaDTO.setPuntaje(respuesta.getPuntaje());
            return respuestaDTO;
        }).collect(Collectors.toList()));
        return dto;
    }

    public PreguntasDTO toDTO2(Preguntas pregunta) {
        PreguntasDTO dto = new PreguntasDTO();
        dto.setId(pregunta.getId());
        dto.setTexto(pregunta.getTexto());
        dto.setRespuestas(
                pregunta.getRespuestas().stream()
                        .map(respuesta -> {
                            RespuestaDTO respuestaDTO = new RespuestaDTO();
                            respuestaDTO.setId(respuesta.getId());
                            respuestaDTO.setTexto(respuesta.getTexto());
                            respuestaDTO.setPuntaje(respuesta.getPuntaje());
                            return respuestaDTO;
                        })
                        .collect(Collectors.toList())
        );
        return dto;
    }
}