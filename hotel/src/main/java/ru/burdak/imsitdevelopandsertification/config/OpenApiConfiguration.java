package ru.burdak.imsitdevelopandsertification.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(info = @Info(title = "Hotel API", version = "0.0.1-SNAPSHOT",
        description = "Поиск свободных номеров по бюджету. Поиск не выполняет бронирование или заселение."))
public class OpenApiConfiguration {
}
