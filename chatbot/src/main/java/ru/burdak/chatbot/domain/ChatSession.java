package ru.burdak.chatbot.domain;

import lombok.Data;

import java.time.Duration;
import java.time.Instant;
import java.util.*;

@Data
public class ChatSession {
    private final UUID sessionId;
    private final Instant createdAt;
    private final List<StoredMessage> messages = new ArrayList<>();

    private SessionState state;
    private Instant lastActivityAt;

    public ChatSession(UUID sessionId, Instant createdAt) {
        this.sessionId = Objects.requireNonNull(sessionId);
        this.createdAt = Objects.requireNonNull(createdAt);
        lastActivityAt = createdAt;
        state = SessionState.ACTIVE;
    }

    public boolean isExpired(Instant now, Duration timeout) {
        return now.isAfter(lastActivityAt.plus(timeout));
    }

    public StoredMessage addMessage(String text, Instant now) {
        ensureActive();

        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException(
                    "Сообщение не должно быть пустым"
            );
        }

        StoredMessage message = new StoredMessage(
                messages.size() + 1,
                text,
                now
        );

        messages.add(message);
        lastActivityAt = now;

        return message;
    }

    public Optional<StoredMessage> findMessage(int number, Instant now) {
        ensureActive();

        if (number <= 0) {
            throw new IllegalArgumentException(
                    "Номер сообщения должен быть положительным"
            );
        }

        if (number > messages.size()) {
            return Optional.empty();
        }

        lastActivityAt = now;
        return Optional.of(messages.get(number - 1));
    }

    public void close() {
        state = SessionState.CLOSED;
        messages.clear();
    }

    private void ensureActive() {
        if (state != SessionState.ACTIVE) {
            throw new IllegalStateException("Сеанс закрыт");
        }
    }

}

