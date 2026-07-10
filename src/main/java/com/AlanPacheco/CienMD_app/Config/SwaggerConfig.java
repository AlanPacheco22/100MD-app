package com.AlanPacheco.CienMD_app.Config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("100 Mexicanos Dijeron — API")
                        .version("2.0")
                        .description("API del juego '100 Mexicanos Dijeron' (Family Feud versión México). Gestiona partidas, preguntas, respuestas, rondas y puntuaciones con soporte WebSocket en tiempo real.")
                        .license(new License().name("Apache 2.0").url("http://springdoc.org")));
    }
}
