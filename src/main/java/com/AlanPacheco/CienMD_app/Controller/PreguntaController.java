package com.AlanPacheco.CienMD_app.Controller;

import com.AlanPacheco.CienMD_app.dto.PreguntasDTO;
import com.AlanPacheco.CienMD_app.Service.PreguntaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin
@RestController
@RequestMapping("/api/preguntas")
public class PreguntaController {

    @Autowired
    private PreguntaService preguntaService;

    @GetMapping
    public List<PreguntasDTO> obtenerTodasLasPreguntas() {
        return preguntaService.listaDePreguntas();
    }

    @GetMapping("/{id}")
    public PreguntasDTO buscarPreguntaPorElId(@PathVariable Long id){
        return preguntaService.buscarPreguntaPorId(id);
    }
}