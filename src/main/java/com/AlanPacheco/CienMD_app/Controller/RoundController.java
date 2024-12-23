package com.AlanPacheco.CienMD_app.Controller;

import com.AlanPacheco.CienMD_app.DTO.RoundDTO;
import com.AlanPacheco.CienMD_app.Entity.GameRound;
import com.AlanPacheco.CienMD_app.Service.GameService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/games/{gameId}/rounds")
public class RoundController {

    @Autowired
    private GameService gameService;

    // Registrar una respuesta en una ronda
    @PostMapping
    public GameRound registerRound(@PathVariable Long gameId, @RequestBody RoundDTO roundDTO) {
        return gameService.registerAnswer(gameId, roundDTO);
    }

    // Obtener los resultados finales de un juego
    @GetMapping("/results")
    public String getFinalResults(@PathVariable Long gameId) {
        return gameService.getFinalResults(gameId);
    }


}