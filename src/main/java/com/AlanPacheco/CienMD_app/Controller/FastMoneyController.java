package com.AlanPacheco.CienMD_app.Controller;

import com.AlanPacheco.CienMD_app.DTO.FastMoneyDTO;
import com.AlanPacheco.CienMD_app.DTO.FastMoneySubmissionDTO;
import com.AlanPacheco.CienMD_app.Service.FastMoneyService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/games/{gameId}/fast-money")
public class FastMoneyController {

    private static final Logger log = LoggerFactory.getLogger(FastMoneyController.class);

    private final FastMoneyService fastMoneyService;

    public FastMoneyController(FastMoneyService fastMoneyService) {
        this.fastMoneyService = fastMoneyService;
    }

    @PostMapping("/start")
    public ResponseEntity<FastMoneyDTO> startFastMoney(
            @PathVariable Long gameId,
            @RequestBody Map<String, Long> body) {
        log.info("[API POST /api/games/{}/fast-money/start] player1={}, player2={}",
                gameId, body.get("player1Id"), body.get("player2Id"));
        FastMoneyDTO result = fastMoneyService.startFastMoney(gameId, body.get("player1Id"), body.get("player2Id"));
        log.info("[API POST /api/games/{}/fast-money/start] Dinero Rápido iniciado", gameId);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/submit")
    public ResponseEntity<FastMoneyDTO> submitAnswers(
            @PathVariable Long gameId,
            @RequestBody @Valid FastMoneySubmissionDTO submission) {
        log.info("[API POST /api/games/{}/fast-money/submit] participantId={}, answers={}",
                gameId, submission.getParticipantId(), submission.getAnswers().size());
        FastMoneyDTO result = fastMoneyService.submitAnswers(gameId, submission);
        log.info("[API POST /api/games/{}/fast-money/submit] Status={}, combinedTotal={}",
                gameId, result.getStatus(), result.getCombinedTotal());
        return ResponseEntity.ok(result);
    }

    @GetMapping("/status")
    public ResponseEntity<FastMoneyDTO> getStatus(@PathVariable Long gameId) {
        log.info("[API GET /api/games/{}/fast-money/status]", gameId);
        return ResponseEntity.ok(fastMoneyService.getFastMoneyStatus(gameId));
    }
}
