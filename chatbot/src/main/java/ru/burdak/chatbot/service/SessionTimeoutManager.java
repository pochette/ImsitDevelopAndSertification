package ru.burdak.chatbot.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class SessionTimeoutManager {
    private final ChatService chatService;

    public SessionTimeoutManager(ChatService chatService) {
        this.chatService = chatService;
    }

    @Scheduled(fixedDelayString = "${chatbot.cleanup-interval:60000}")
    public void cleanupExpiredSessions() {
        chatService.cleanupExpiredSessions();
    }
}

