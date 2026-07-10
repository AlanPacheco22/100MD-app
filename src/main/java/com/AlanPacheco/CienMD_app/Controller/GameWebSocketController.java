package com.AlanPacheco.CienMD_app.Controller;

import com.AlanPacheco.CienMD_app.DTO.RoundDTO;
import com.AlanPacheco.CienMD_app.Service.GameService;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

@Controller
public class GameWebSocketController {

    private final GameService gameService;

    public GameWebSocketController(GameService gameService) {
        this.gameService = gameService;
    }

    @MessageMapping("/game/{gameId}/answer")
    public void handleAnswer(@DestinationVariable Long gameId, @Payload RoundDTO roundDTO) {
        gameService.submitAnswer(gameId, roundDTO);
    }

    @MessageMapping("/game/{gameId}/start")
    public void handleStartRound(@DestinationVariable Long gameId) {
        gameService.startNextRound(gameId);
    }

    @MessageMapping("/game/{gameId}/end")
    public void handleEndRound(@DestinationVariable Long gameId) {
        gameService.endRound(gameId);
    }

    @MessageMapping("/game/{gameId}/pass")
    public void handlePass(@DestinationVariable Long gameId) {
        gameService.passTurn(gameId);
    }
}
