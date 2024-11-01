package com.AlanPacheco.CienMD_app.Service;

import com.AlanPacheco.CienMD_app.Mapper.PreguntaMapper;
import com.AlanPacheco.CienMD_app.Model.Preguntas;
import com.AlanPacheco.CienMD_app.Repository.PreguntaRepository;
import com.AlanPacheco.CienMD_app.dto.PreguntasDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PreguntaService {

    @Autowired
    private PreguntaRepository preguntaRepository;

    @Autowired
    private PreguntaMapper preguntaMapper;

    public List<PreguntasDTO> listaDePreguntas (){
        return preguntaRepository.findAll()
                .stream()
                .map(preguntaMapper::toDTO)
                .collect(Collectors.toList());
    }

    public PreguntasDTO buscarPreguntaPorId(Long id) {
        Preguntas pregunta = preguntaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pregunta no encontrada"));
        return preguntaMapper.toDTO2(pregunta);
    }

}