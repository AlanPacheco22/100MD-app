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
                        .title("API de 100 Mexicanos Dijeron")
                        .version("1.0")
                        .description("Documentación interactiva para probar los endpoints del juego")
                        .license(new License().name("Apache 2.0").url("http://springdoc.org")));
    }
}
