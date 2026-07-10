package com.AlanPacheco.CienMD_app.Controller;

import com.AlanPacheco.CienMD_app.DTO.GameDTO;
import com.AlanPacheco.CienMD_app.DTO.GameQuestionDTO;
import com.AlanPacheco.CienMD_app.DTO.GameResultsDTO;
import com.AlanPacheco.CienMD_app.DTO.RoundDTO;
import com.AlanPacheco.CienMD_app.Service.GameService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/games")
public class GameController {

    private final GameService gameService;

    public GameController(GameService gameService) {
        this.gameService = gameService;
    }

    @PostMapping
    public ResponseEntity<GameDTO> createGame() {
        return ResponseEntity.status(HttpStatus.CREATED).body(gameService.createNewGame());
    }

    @GetMapping
    public ResponseEntity<List<GameDTO>> getAllGames() {
        return ResponseEntity.ok(gameService.getAllGames());
    }

    @GetMapping("/{id}")
    public ResponseEntity<GameDTO> getGameById(@PathVariable Long id) {
        return ResponseEntity.ok(gameService.getGameById(id));
    }

    @GetMapping("/{gameId}/results")
    public ResponseEntity<GameResultsDTO> getResults(@PathVariable Long gameId) {
        return ResponseEntity.ok(gameService.getFinalResults(gameId));
    }

    @GetMapping("/{gameId}/questions/{questionId}")
    public ResponseEntity<GameQuestionDTO> getGameQuestion(
            @PathVariable Long gameId, @PathVariable Long questionId) {
        return ResponseEntity.ok(gameService.getGameQuestion(gameId, questionId));
    }

    @PostMapping("/{gameId}/rounds/start")
    public ResponseEntity<GameDTO> startRound(@PathVariable Long gameId) {
        return ResponseEntity.ok(gameService.startNextRound(gameId));
    }

    @PostMapping("/{gameId}/rounds/answer")
    public ResponseEntity<GameQuestionDTO> submitAnswer(
            @PathVariable Long gameId,
            @RequestBody @Valid RoundDTO roundDTO) {
        return ResponseEntity.ok(gameService.submitAnswer(gameId, roundDTO));
    }

    @PostMapping("/{gameId}/rounds/end")
    public ResponseEntity<GameDTO> endRound(@PathVariable Long gameId) {
        return ResponseEntity.ok(gameService.endRound(gameId));
    }

    @PostMapping("/{gameId}/rounds/pass")
    public ResponseEntity<GameDTO> passTurn(@PathVariable Long gameId) {
        return ResponseEntity.ok(gameService.passTurn(gameId));
    }
}
