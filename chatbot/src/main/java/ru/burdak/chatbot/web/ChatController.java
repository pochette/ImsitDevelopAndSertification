package ru.burdak.chatbot.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import ru.burdak.chatbot.domain.StoredMessage;
import ru.burdak.chatbot.service.ChatService;

import java.net.URI;
import java.util.UUID;

@Tag(name = "Сеансы и сообщения")
@RestController
@RequestMapping("/api/sessions")
public class ChatController {
    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @Operation(summary = "Создать сеанс", description = "Создаёт независимый сеанс с UUID и нулём сообщений.")
    @ApiResponse(responseCode = "201", description = "Сеанс создан")
    @PostMapping
    public ResponseEntity<ChatService.SessionView> login() {
        var session = chatService.login();
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(session.sessionId()).toUri();
        return ResponseEntity.created(location).body(session);
    }

    @Operation(summary = "Получить состояние сеанса", description = "Просмотр состояния не продлевает срок неактивности.")
    @ApiResponse(responseCode = "200", description = "Состояние сеанса")
    @ApiResponse(responseCode = "400", description = "Некорректный UUID", content = @Content(schema = @Schema(implementation = ApiExceptionHandler.ApiError.class)))
    @ApiResponse(responseCode = "404", description = "Сеанс отсутствует или истёк", content = @Content(schema = @Schema(implementation = ApiExceptionHandler.ApiError.class)))
    @GetMapping("/{sessionId}")
    public ChatService.SessionView getSession(@PathVariable UUID sessionId) {
        return chatService.getSession(sessionId);
    }

    @Operation(summary = "Сохранить сообщение", description = "Нумерация начинается с 1 в каждом сеансе. Успешная запись продлевает срок неактивности. По умолчанию: до 1000 сообщений, до 4096 кодовых точек Unicode в тексте; ограничения настраиваются.")
    @ApiResponse(responseCode = "201", description = "Сообщение сохранено")
    @ApiResponse(responseCode = "400", description = "Неверный UUID, JSON или пустой/слишком длинный текст", content = @Content(schema = @Schema(implementation = ApiExceptionHandler.ApiError.class)))
    @ApiResponse(responseCode = "404", description = "Сеанс отсутствует или истёк", content = @Content(schema = @Schema(implementation = ApiExceptionHandler.ApiError.class)))
    @ApiResponse(responseCode = "409", description = "Достигнут лимит сообщений", content = @Content(schema = @Schema(implementation = ApiExceptionHandler.ApiError.class)))
    @ApiResponse(responseCode = "415", description = "Ожидается application/json", content = @Content(schema = @Schema(implementation = ApiExceptionHandler.ApiError.class)))
    @PostMapping(value = "/{sessionId}/messages", consumes = "application/json")
    public ResponseEntity<StoredMessage> sendMessage(@PathVariable UUID sessionId,
                                                     @RequestBody SendMessageRequest request) {
        StoredMessage message = chatService.sendMessage(sessionId, request.text());
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{number}").buildAndExpand(message.number()).toUri();
        return ResponseEntity.created(location).body(message);
    }

    @Operation(summary = "Получить сообщение по номеру", description = "Успешное чтение сообщения продлевает срок неактивности.")
    @ApiResponse(responseCode = "200", description = "Сохранённое сообщение")
    @ApiResponse(responseCode = "400", description = "Неверный UUID или номер сообщения", content = @Content(schema = @Schema(implementation = ApiExceptionHandler.ApiError.class)))
    @ApiResponse(responseCode = "404", description = "Сеанс или сообщение не найдено", content = @Content(schema = @Schema(implementation = ApiExceptionHandler.ApiError.class)))
    @GetMapping("/{sessionId}/messages/{number}")
    public StoredMessage remember(@PathVariable UUID sessionId, @Parameter(description = "Положительный номер сообщения", example = "1") @PathVariable int number) {
        return chatService.remember(sessionId, number);
    }

    @Operation(summary = "Завершить сеанс", description = "Удаляет сеанс и сообщения. Повторное удаление корректного UUID также возвращает 204.")
    @ApiResponse(responseCode = "204", description = "Сеанс удалён или уже отсутствует", content = @Content)
    @ApiResponse(responseCode = "400", description = "Некорректный UUID", content = @Content(schema = @Schema(implementation = ApiExceptionHandler.ApiError.class)))
    @DeleteMapping("/{sessionId}")
    public ResponseEntity<Void> logout(@PathVariable UUID sessionId) {
        chatService.logout(sessionId);
        return ResponseEntity.noContent().build();
    }

    public record SendMessageRequest(
            @Schema(description = "Непустой текст; лимит задаёт chatbot.max-message-length", example = "Привет, бот!", requiredMode = Schema.RequiredMode.REQUIRED) String text) {
    }
}

