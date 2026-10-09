package ru.burdak.chatbot.web;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import ru.burdak.chatbot.domain.StoredMessage;
import ru.burdak.chatbot.service.ChatService;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/api/sessions")
public class ChatController {
    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping
    public ResponseEntity<ChatService.SessionView> login() {
        var session = chatService.login();
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(session.sessionId()).toUri();
        return ResponseEntity.created(location).body(session);
    }

    @GetMapping("/{sessionId}")
    public ChatService.SessionView getSession(@PathVariable UUID sessionId) {
        return chatService.getSession(sessionId);
    }

    @PostMapping(value = "/{sessionId}/messages", consumes = "application/json")
    public ResponseEntity<StoredMessage> sendMessage(@PathVariable UUID sessionId,
                                                     @RequestBody SendMessageRequest request) {
        StoredMessage message = chatService.sendMessage(sessionId, request.text());
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{number}").buildAndExpand(message.number()).toUri();
        return ResponseEntity.created(location).body(message);
    }

    @GetMapping("/{sessionId}/messages/{number}")
    public StoredMessage remember(@PathVariable UUID sessionId, @PathVariable int number) {
        return chatService.remember(sessionId, number);
    }

    @DeleteMapping("/{sessionId}")
    public ResponseEntity<Void> logout(@PathVariable UUID sessionId) {
        chatService.logout(sessionId);
        return ResponseEntity.noContent().build();
    }

    public record SendMessageRequest(String text) {
    }
}

