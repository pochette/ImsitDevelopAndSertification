package ru.burdak.chatbot.service;

import org.junit.jupiter.api.Test;
import ru.burdak.chatbot.repository.InMemorySessionRepository;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;

class ChatServiceTest {
    private final ChatService service = new ChatService(
            new InMemorySessionRepository(),
            Clock.fixed(Instant.parse("2026-10-09T10:00:00Z"), ZoneOffset.UTC),
            Duration.ofMinutes(15), 1000, 4096);

    @Test
    void createsStoresAndRemembersMessage() {
        var session = service.login();
        var saved = service.sendMessage(session.sessionId(), "Привет 🌍");

        assertEquals(1, saved.number());
        assertEquals("Привет 🌍", service.remember(session.sessionId(), 1).text());
        assertEquals(1, service.getSession(session.sessionId()).messageCount());
    }

    @Test
    void closedSessionCannotBeUsed() {
        var session = service.login();
        service.logout(session.sessionId());

        assertThrows(NoSuchElementException.class,
                () -> service.sendMessage(session.sessionId(), "Привет"));
        assertDoesNotThrow(() -> service.logout(session.sessionId()));
    }

    @Test
    void rejectsBlankAndInvalidMessageNumber() {
        var session = service.login();

        assertThrows(IllegalArgumentException.class,
                () -> service.sendMessage(session.sessionId(), "  "));
        assertThrows(IllegalArgumentException.class,
                () -> service.remember(session.sessionId(), 0));
    }

    @Test
    void messageLimitReturnsConflictCondition() {
        var limitedService = new ChatService(
                new InMemorySessionRepository(), Clock.systemUTC(),
                Duration.ofMinutes(15), 1, 4096);
        var session = limitedService.login();
        limitedService.sendMessage(session.sessionId(), "one");

        assertThrows(ChatService.MessageLimitExceededException.class,
                () -> limitedService.sendMessage(session.sessionId(), "two"));
    }
}

