package com.AlanPacheco.CienMD_app.Config;

import com.AlanPacheco.CienMD_app.DTO.GameDTO;
import com.AlanPacheco.CienMD_app.DTO.GameUpdateDTO;
import com.AlanPacheco.CienMD_app.Service.GameService;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
public class WebSocketEventListener {

    private final SimpMessagingTemplate messagingTemplate;
    private final GameService gameService;

    public WebSocketEventListener(SimpMessagingTemplate messagingTemplate, GameService gameService) {
        this.messagingTemplate = messagingTemplate;
        this.gameService = gameService;
    }

    @EventListener
    public void handleGameEvent(GameEvent event) {
        try {
            GameDTO gameState = gameService.getGameById(event.getGameId());
            GameUpdateDTO update = new GameUpdateDTO(event.getType(), gameState);
            messagingTemplate.convertAndSend("/topic/game/" + event.getGameId(), update);
        } catch (Exception e) {
            System.err.println("Error broadcasting game event: " + e.getMessage());
        }
    }
}
