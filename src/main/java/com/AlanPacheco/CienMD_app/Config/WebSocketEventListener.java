package com.AlanPacheco.CienMD_app.Config;

import com.AlanPacheco.CienMD_app.DTO.GameDTO;
import com.AlanPacheco.CienMD_app.DTO.GameUpdateDTO;
import com.AlanPacheco.CienMD_app.Service.GameService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
public class WebSocketEventListener {

    private static final Logger log = LoggerFactory.getLogger(WebSocketEventListener.class);

    private final SimpMessagingTemplate messagingTemplate;
    private final GameService gameService;

    public WebSocketEventListener(SimpMessagingTemplate messagingTemplate, GameService gameService) {
        this.messagingTemplate = messagingTemplate;
        this.gameService = gameService;
    }

    @EventListener
    public void handleGameEvent(GameEvent event) {
        log.info("[WebSocket] Evento recibido: type={}, gameId={}", event.getType(), event.getGameId());
        try {
            GameDTO gameState = gameService.getGameById(event.getGameId());
            GameUpdateDTO update = new GameUpdateDTO(event.getType(), gameState);
            messagingTemplate.convertAndSend("/topic/game/" + event.getGameId(), update);
            log.info("[WebSocket] Broadcast enviado a /topic/game/{}: event={}, status={}, t1={}, t2={}",
                    event.getGameId(), event.getType(), gameState.getStatus(), gameState.getTeam1Score(), gameState.getTeam2Score());
        } catch (Exception e) {
            log.error("[WebSocket] Error broadcastando evento type={}, gameId={}: {}", event.getType(), event.getGameId(), e.getMessage(), e);
        }
    }
}
