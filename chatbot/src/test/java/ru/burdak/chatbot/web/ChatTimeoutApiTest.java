package ru.burdak.chatbot.web;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Тайм-аут сеанса через HTTP API: управляемые часы вместо ожидания 15 минут.
 */
@SpringBootTest(properties = {"chatbot.cleanup-interval=3600000", "chatbot.session-timeout=15m"})
@AutoConfigureMockMvc
class ChatTimeoutApiTest {
  private static final Instant START = Instant.parse("2026-10-23T10:00:00Z");
  private static final Duration TIMEOUT = Duration.ofMinutes(15);

  @Autowired private MockMvc mvc;
  @Autowired private ObjectMapper objectMapper;
  @Autowired private MutableClock clock;

  @BeforeEach
  void resetClock() {
    clock.set(START);
  }

  @Test
  void expiresExactlyAtTimeoutAndErrorsDoNotExtendSession() throws Exception {
    String id = createSession();
    sendText(id, "Привет").andExpect(status().isCreated());

    clock.set(START.plus(TIMEOUT).minusSeconds(1));
    mvc.perform(get("/api/sessions/{id}", id))
      .andExpect(status().isOk());
    mvc.perform(get("/api/sessions/{id}/messages/{number}", id, 5))
      .andExpect(status().isNotFound());
    sendText(id, " ").andExpect(status().isBadRequest());

    // Чтение статуса и ошибочные запросы не продлили сеанс
    clock.set(START.plus(TIMEOUT));
    mvc.perform(get("/api/sessions/{id}", id))
      .andExpect(status().isNotFound())
      .andExpect(jsonPath("$.code").value("SESSION_NOT_FOUND"));
  }

  @Test
  void successfulWriteAndReadExtendSession() throws Exception {
    String id = createSession();

    clock.set(START.plus(Duration.ofMinutes(10)));
    sendText(id, "Привет").andExpect(status().isCreated());

    clock.set(START.plus(Duration.ofMinutes(20)));
    mvc.perform(get("/api/sessions/{id}/messages/{number}", id, 1))
      .andExpect(status().isOk());

    Instant lastActivity = START.plus(Duration.ofMinutes(20));
    clock.set(lastActivity.plus(TIMEOUT).minusSeconds(1));
    mvc.perform(get("/api/sessions/{id}", id))
      .andExpect(status().isOk());

    clock.set(lastActivity.plus(TIMEOUT));
    mvc.perform(get("/api/sessions/{id}", id))
      .andExpect(status().isNotFound());
  }

  private String createSession() throws Exception {
    String body = mvc.perform(post("/api/sessions"))
      .andExpect(status().isCreated())
      .andReturn().getResponse().getContentAsString();
    return objectMapper.readTree(body).get("sessionId").asString();
  }

  private org.springframework.test.web.servlet.ResultActions sendText(String id, String text) throws Exception {
    return mvc.perform(post("/api/sessions/{id}/messages", id)
                         .contentType(MediaType.APPLICATION_JSON)
                         .content("{\"text\":\"" + text + "\"}"));
  }

  @TestConfiguration
  static class ClockConfiguration {
    @Bean
    @Primary
    MutableClock testClock() {
      return new MutableClock(START);
    }
  }

  static class MutableClock extends Clock {
    private volatile Instant now;

    MutableClock(Instant now) {
      this.now = now;
    }

    void set(Instant now) {
      this.now = now;
    }

    @Override
    public ZoneId getZone() {
      return ZoneOffset.UTC;
    }

    @Override
    public Clock withZone(ZoneId zone) {
      return this;
    }

    @Override
    public Instant instant() {
      return now;
    }
  }
}