package com.AlanPacheco.CienMD_app.Controller;

import com.AlanPacheco.CienMD_app.DTO.GameResultsDTO;
import com.AlanPacheco.CienMD_app.DTO.RoundDTO;
import com.AlanPacheco.CienMD_app.Service.GameService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/games/{gameId}/rounds")
@Deprecated
public class RoundController {

    private final GameService gameService;

    public RoundController(GameService gameService) {
        this.gameService = gameService;
    }

    @PostMapping
    public ResponseEntity<String> registerRound(@PathVariable Long gameId, @RequestBody RoundDTO roundDTO) {
        return ResponseEntity.ok("Usa POST /api/games/{id}/rounds/answer en su lugar");
    }

    @GetMapping("/results")
    public ResponseEntity<GameResultsDTO> getFinalResults(@PathVariable Long gameId) {
        return ResponseEntity.ok(gameService.getFinalResults(gameId));
    }
}
