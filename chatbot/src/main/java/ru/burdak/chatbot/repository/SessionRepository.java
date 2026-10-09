package ru.burdak.chatbot.repository;

import ru.burdak.chatbot.domain.ChatSession;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

public interface SessionRepository {
    UUID create(Instant now);

    /**
     * Checks expiration and executes an operation atomically for this session.
     * The operation must return an immutable result, never the session itself,
     * and must not call repository methods recursively.
     */
    <T> Optional<T> withActiveSession(UUID id, Instant now, Duration timeout,
                                     Function<ChatSession, T> operation);

    void delete(UUID id);

    void deleteExpired(Instant now, Duration timeout);
}
