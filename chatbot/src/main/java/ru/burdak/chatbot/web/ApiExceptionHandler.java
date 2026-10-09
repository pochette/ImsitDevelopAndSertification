package ru.burdak.chatbot.web;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.burdak.chatbot.service.ChatService;

import java.util.NoSuchElementException;

@RestControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiError> badRequest(IllegalArgumentException exception) {
        return error(HttpStatus.BAD_REQUEST, "INVALID_REQUEST", exception.getMessage());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> invalidJson() {
        return error(HttpStatus.BAD_REQUEST, "INVALID_REQUEST", "Некорректное тело запроса");
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ApiError> unsupportedMediaType() {
        return error(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "UNSUPPORTED_MEDIA_TYPE",
                "Ожидается Content-Type application/json");
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiError> invalidParameter() {
        return error(HttpStatus.BAD_REQUEST, "INVALID_REQUEST",
                "Некорректный идентификатор или номер сообщения");
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<ApiError> notFound(NoSuchElementException exception) {
        String code = exception.getMessage().startsWith("Сообщение")
                ? "MESSAGE_NOT_FOUND" : "SESSION_NOT_FOUND";
        return error(HttpStatus.NOT_FOUND, code, exception.getMessage());
    }

    @ExceptionHandler(ChatService.MessageLimitExceededException.class)
    public ResponseEntity<ApiError> messageLimit() {
        return error(HttpStatus.CONFLICT, "MESSAGE_LIMIT_REACHED",
                "Достигнут лимит сообщений в сеансе");
    }

    private ResponseEntity<ApiError> error(HttpStatus status, String code, String message) {
        return ResponseEntity.status(status).body(new ApiError(code, message));
    }

    public record ApiError(String code, String message) {
    }
}
