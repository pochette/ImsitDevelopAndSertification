package ru.burdak.chatbot.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.burdak.chatbot.domain.ChatSession;
import ru.burdak.chatbot.domain.SessionState;
import ru.burdak.chatbot.domain.StoredMessage;
import ru.burdak.chatbot.repository.SessionRepository;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
public class ChatService {
    private final SessionRepository sessions;
    private final Clock clock;
    private final Duration timeout;
    private final int maxMessages;
    private final int maxMessageLength;

    public ChatService(
            SessionRepository sessions,
            Clock clock,
            @Value("${chatbot.session-timeout:15m}") Duration timeout,
            @Value("${chatbot.max-messages:1000}") int maxMessages,
            @Value("${chatbot.max-message-length:4096}") int maxMessageLength) {
        this.sessions = sessions;
        this.clock = clock;
        this.timeout = timeout;
        this.maxMessages = maxMessages;
        this.maxMessageLength = maxMessageLength;
    }

    public SessionView login() {
        Instant now = clock.instant();
        UUID id = sessions.create(now);
        return new SessionView(id, SessionState.ACTIVE, 0, now, now.plus(timeout));
    }

    public SessionView getSession(UUID id) {
        Instant now = clock.instant();
        return withSession(id, now, session -> new SessionView(
                id, session.getState(), session.getMessageCount(),
                session.getCreatedAt(), session.getLastActivityAt().plus(timeout)));
    }

    public StoredMessage sendMessage(UUID id, String text) {
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException("Сообщение не должно быть пустым");
        }
        if (text.codePointCount(0, text.length()) > maxMessageLength) {
            throw new IllegalArgumentException("Превышена максимальная длина сообщения");
        }
        Instant now = clock.instant();
        return withSession(id, now, session -> {
            if (session.getMessageCount() >= maxMessages) {
                throw new MessageLimitExceededException();
            }
            return session.addMessage(text, now);
        });
    }

    public StoredMessage remember(UUID id, int number) {
        Instant now = clock.instant();
        return withSession(id, now, session -> {
            if (number <= 0) {
                throw new IllegalArgumentException("Номер сообщения должен быть положительным");
            }
            return session.findMessage(number, now)
                    .orElseThrow(() -> new NoSuchElementException("Сообщение не найдено"));
        });
    }

    public void logout(UUID id) {
        sessions.delete(id);
    }

    public void cleanupExpiredSessions() {
        sessions.deleteExpired(clock.instant(), timeout);
    }

    private <T> T withSession(UUID id, Instant now,
                              java.util.function.Function<ChatSession, T> operation) {
        return sessions.withActiveSession(id, now, timeout, operation)
                .orElseThrow(() -> new NoSuchElementException("Сеанс не найден или истёк"));
    }

    public record SessionView(UUID sessionId, SessionState state, int messageCount,
                              Instant createdAt, Instant expiresAt) {
    }

    public static class MessageLimitExceededException extends RuntimeException {
    }
}

