package com.AlanPacheco.CienMD_app.Controller;


import com.AlanPacheco.CienMD_app.DTO.GameUpdateDTO;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
public class GameWebSocketController {

    // Maneja mensajes enviados a "/app/updateGame"
    @MessageMapping("/updateGame")
    @SendTo("/topic/gameUpdates")
    public GameUpdateDTO updateGame(GameUpdateDTO gameUpdate) {
        // Procesar actualizaciones del juego
        // Aquí puedes añadir lógica para validar la actualización si es necesario
        return gameUpdate;
    }
}
