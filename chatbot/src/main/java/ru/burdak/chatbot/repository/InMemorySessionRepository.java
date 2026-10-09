package ru.burdak.chatbot.repository;

import org.springframework.stereotype.Repository;
import ru.burdak.chatbot.domain.ChatSession;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

@Repository
public class InMemorySessionRepository implements SessionRepository {
    private final ConcurrentHashMap<UUID, ChatSession> sessions = new ConcurrentHashMap<>();

    @Override
    public UUID create(Instant now) {
        UUID id;
        do {
            id = UUID.randomUUID();
        } while (sessions.putIfAbsent(id, new ChatSession(id, now)) != null);
        return id;
    }

    @Override
    public <T> Optional<T> withActiveSession(UUID id, Instant now, Duration timeout,
                                            Function<ChatSession, T> operation) {
        Objects.requireNonNull(operation);
        AtomicReference<T> result = new AtomicReference<>();
        sessions.computeIfPresent(id, (key, session) -> {
            if (session.isExpired(now, timeout)) {
                session.close();
                return null;
            }
            result.set(Objects.requireNonNull(operation.apply(session)));
            return session;
        });
        return Optional.ofNullable(result.get());
    }

    @Override
    public void delete(UUID id) {
        sessions.computeIfPresent(id, (key, session) -> {
            session.close();
            return null;
        });
    }

    @Override
    public void deleteExpired(Instant now, Duration timeout) {
        sessions.forEach((id, ignored) -> sessions.computeIfPresent(id, (key, session) -> {
            if (session.isExpired(now, timeout)) {
                session.close();
                return null;
            }
            return session;
        }));
    }
}
