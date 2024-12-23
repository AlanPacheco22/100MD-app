package com.AlanPacheco.CienMD_app.Config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Configurar el prefijo para los mensajes enviados desde el servidor
        config.enableSimpleBroker("/topic");
        // Prefijo para mensajes enviados al servidor
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Registrar el endpoint para los clientes WebSocket
        registry.addEndpoint("/ws")
                .setAllowedOrigins("*")  // Permitir orígenes cruzados para pruebas
                .withSockJS();           // Activar soporte para SockJS
    }
}
