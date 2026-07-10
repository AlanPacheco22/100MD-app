package com.AlanPacheco.CienMD_app.Controller;

import com.AlanPacheco.CienMD_app.DTO.CreateParticipantDTO;
import com.AlanPacheco.CienMD_app.DTO.ParticipantDTO;
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
@RequestMapping("/api/games/{gameId}/participants")
public class ParticipantController {

    private final GameService gameService;

    public ParticipantController(GameService gameService) {
        this.gameService = gameService;
    }

    @PostMapping
    public ResponseEntity<ParticipantDTO> addParticipant(
            @PathVariable Long gameId,
            @RequestBody @Valid CreateParticipantDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(gameService.addParticipant(gameId, dto));
    }

    @GetMapping
    public ResponseEntity<List<ParticipantDTO>> getParticipants(@PathVariable Long gameId) {
        return ResponseEntity.ok(gameService.getParticipants(gameId));
    }
}
