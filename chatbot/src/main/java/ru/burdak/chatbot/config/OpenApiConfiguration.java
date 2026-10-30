package ru.burdak.chatbot.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(info = @Info(title = "Chatbot API", version = "0.0.1-SNAPSHOT",
        description = "Сеансы и хранение сообщений. Данные находятся в памяти; ответы ИИ не генерируются."))
public class OpenApiConfiguration {
}
