package com.AlanPacheco.CienMD_app.Controller;

import com.AlanPacheco.CienMD_app.DTO.GameUpdateDTO;
import com.AlanPacheco.CienMD_app.DTO.RoundDTO;
import com.AlanPacheco.CienMD_app.Service.GameService;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
public class GameWebSocketController {

    private final GameService gameService;
    private final SimpMessagingTemplate messagingTemplate;

    public GameWebSocketController(GameService gameService, SimpMessagingTemplate messagingTemplate) {
        this.gameService = gameService;
        this.messagingTemplate = messagingTemplate;
    }

    @MessageMapping("/game/{gameId}/answer")
    public void handleAnswer(@DestinationVariable Long gameId, @Payload RoundDTO roundDTO) {
        var result = gameService.submitAnswer(gameId, roundDTO);
        var gameState = gameService.getGameById(gameId);
        messagingTemplate.convertAndSend("/topic/game/" + gameId,
                new GameUpdateDTO("ANSWER_SUBMITTED", gameState));
    }

    @MessageMapping("/game/{gameId}/start")
    public void handleStartRound(@DestinationVariable Long gameId) {
        var gameState = gameService.startNextRound(gameId);
        messagingTemplate.convertAndSend("/topic/game/" + gameId,
                new GameUpdateDTO("ROUND_STARTED", gameState));
    }

    @MessageMapping("/game/{gameId}/end")
    public void handleEndRound(@DestinationVariable Long gameId) {
        var gameState = gameService.endRound(gameId);
        messagingTemplate.convertAndSend("/topic/game/" + gameId,
                new GameUpdateDTO("ROUND_ENDED", gameState));
    }
}
