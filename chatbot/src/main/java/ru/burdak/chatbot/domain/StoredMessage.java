package ru.burdak.chatbot.domain;

import java.time.Instant;

public record StoredMessage(
        int number,
        String text,
        Instant receivedAt
) {}
