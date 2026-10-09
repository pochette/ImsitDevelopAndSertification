package ru.burdak.chatbot.repository;

import org.junit.jupiter.api.Test;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.concurrent.*;
import static org.junit.jupiter.api.Assertions.*;

class InMemorySessionRepositoryTest {
    private final InMemorySessionRepository repository = new InMemorySessionRepository();
    private final Instant now = Instant.parse("2026-09-25T10:00:00Z");
    private final Duration timeout = Duration.ofMinutes(15);

    @Test
    void storesMessagesAndProtectsHistory() {
        var id = repository.create(now);
        repository.withActiveSession(id, now, timeout, s -> s.addMessage("Hello", now));
        var history = repository.withActiveSession(id, now, timeout, s -> s.getMessages()).orElseThrow();
        assertEquals("Hello", history.getFirst().text());
        assertThrows(UnsupportedOperationException.class, history::clear);
        assertEquals(1, repository.withActiveSession(id, now, timeout, s -> s.getMessages().size()).orElseThrow());
    }

    @Test
    void expiresExactlyAtDeadlineAndDoesNotCallOperation() {
        var id = repository.create(now);
        assertTrue(repository.withActiveSession(id, now.plus(timeout), timeout, s -> {
            fail("Expired session must not be accessed");
            return true;
        }).isEmpty());
    }

    @Test
    void activityExtendsDeadlineAndCleanupKeepsActiveSession() {
        var id = repository.create(now);
        var later = now.plusSeconds(60);
        repository.withActiveSession(id, later, timeout, s -> s.addMessage("Hello", later));
        repository.deleteExpired(now.plus(timeout), timeout);
        assertTrue(repository.withActiveSession(id, now.plus(timeout), timeout, s -> s.getSessionId()).isPresent());
        repository.deleteExpired(later.plus(timeout), timeout);
        assertTrue(repository.withActiveSession(id, later.plus(timeout), timeout, s -> s.getSessionId()).isEmpty());
    }

    @Test
    void deleteIsIdempotentAndSessionsAreIsolated() {
        var first = repository.create(now);
        var second = repository.create(now);
        repository.delete(first);
        repository.delete(first);
        assertTrue(repository.withActiveSession(first, now, timeout, s -> s.getSessionId()).isEmpty());
        assertEquals(0, repository.withActiveSession(second, now, timeout, s -> s.getMessages().size()).orElseThrow());
    }

    @Test
    void concurrentWritesHaveUniqueSequentialNumbers() throws Exception {
        var id = repository.create(now);
        var pool = Executors.newFixedThreadPool(4);
        try {
            var tasks = new ArrayList<Callable<Integer>>();
            for (int i = 0; i < 100; i++) {
                tasks.add(() -> repository.withActiveSession(id, now, timeout,
                        s -> s.addMessage("text", now).number()).orElseThrow());
            }
            var numbers = new java.util.HashSet<Integer>();
            for (var result : pool.invokeAll(tasks, 10, TimeUnit.SECONDS)) {
                numbers.add(result.get());
            }
            assertEquals(100, numbers.size());
            assertTrue(numbers.contains(1));
            assertTrue(numbers.contains(100));
        } finally {
            pool.shutdownNow();
        }
    }
}
