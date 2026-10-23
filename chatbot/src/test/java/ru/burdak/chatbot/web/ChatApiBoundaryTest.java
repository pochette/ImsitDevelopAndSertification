package ru.burdak.chatbot.web;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import tools.jackson.databind.ObjectMapper;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Граничные значения и негативные сценарии API (Negative Testing, Black Box Testing).
 * Лимит сообщений уменьшен до 3, чтобы проверить ответ 409 без отправки 1000 сообщений.
 */
@SpringBootTest(properties = {"chatbot.cleanup-interval=3600000", "chatbot.max-messages=3"})
@AutoConfigureMockMvc
class ChatApiBoundaryTest {
  private static final int MAX_LENGTH = 4096;

  @Autowired private MockMvc mvc;
  @Autowired private ObjectMapper objectMapper;

  @Test
  void acceptsTextOfMaxLengthCountedInCodePoints() throws Exception {
    String id = createSession();

    send(id, textJson("a".repeat(MAX_LENGTH)))
      .andExpect(status().isCreated());
    // 4096 эмодзи = 4096 кодовых точек Unicode, но 8192 символа char
    send(id, textJson("🌍".repeat(MAX_LENGTH)))
      .andExpect(status().isCreated());
  }

  @Test
  void rejectsTextLongerThanMaxLength() throws Exception {
    String id = createSession();

    send(id, textJson("a".repeat(MAX_LENGTH + 1)))
      .andExpect(status().isBadRequest())
      .andExpect(jsonPath("$.code").value("INVALID_REQUEST"));
    send(id, textJson("🌍".repeat(MAX_LENGTH + 1)))
      .andExpect(status().isBadRequest());

    mvc.perform(get("/api/sessions/{id}", id))
      .andExpect(jsonPath("$.messageCount").value(0));
  }

  @ParameterizedTest
  @ValueSource(strings = {"{\"text\":null}", "{}", "{\"text\":\"\"}", "{\"text\":", "not json"})
  void rejectsMissingEmptyOrMalformedText(String json) throws Exception {
    String id = createSession();

    send(id, json)
      .andExpect(status().isBadRequest())
      .andExpect(jsonPath("$.code").value("INVALID_REQUEST"));
  }

  @Test
  void returnsConflictWhenMessageLimitReached() throws Exception {
    String id = createSession();
    for (int number = 1; number <= 3; number++) {
      send(id, textJson("Сообщение " + number))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.number").value(number));
    }

    send(id, textJson("Лишнее сообщение"))
      .andExpect(status().isConflict())
      .andExpect(jsonPath("$.code").value("MESSAGE_LIMIT_REACHED"));
    mvc.perform(get("/api/sessions/{id}", id))
      .andExpect(jsonPath("$.messageCount").value(3));
  }

  @ParameterizedTest
  @ValueSource(strings = {"0", "-1", "abc"})
  void rejectsInvalidMessageNumber(String number) throws Exception {
    String id = createSession();
    send(id, textJson("Привет")).andExpect(status().isCreated());

    mvc.perform(get("/api/sessions/{id}/messages/{number}", id, number))
      .andExpect(status().isBadRequest())
      .andExpect(jsonPath("$.code").value("INVALID_REQUEST"));
  }

  @Test
  void rejectsSessionIdThatIsNotUuid() throws Exception {
    mvc.perform(get("/api/sessions/{id}", "not-a-uuid"))
      .andExpect(status().isBadRequest())
      .andExpect(jsonPath("$.code").value("INVALID_REQUEST"));
    mvc.perform(delete("/api/sessions/{id}", "12345"))
      .andExpect(status().isBadRequest());
  }

  @Test
  void returnsNotFoundForUnknownSession() throws Exception {
    UUID unknown = UUID.randomUUID();

    mvc.perform(get("/api/sessions/{id}", unknown))
      .andExpect(status().isNotFound())
      .andExpect(jsonPath("$.code").value("SESSION_NOT_FOUND"));
    send(unknown.toString(), textJson("Привет"))
      .andExpect(status().isNotFound())
      .andExpect(jsonPath("$.code").value("SESSION_NOT_FOUND"));
    mvc.perform(get("/api/sessions/{id}/messages/{number}", unknown, 1))
      .andExpect(status().isNotFound())
      .andExpect(jsonPath("$.code").value("SESSION_NOT_FOUND"));
  }

  @Test
  void isolatesNumberingAndHistoryBetweenSessions() throws Exception {
    String first = createSession();
    String second = createSession();

    send(first, textJson("Сеанс A")).andExpect(jsonPath("$.number").value(1));
    send(second, textJson("Сеанс B")).andExpect(jsonPath("$.number").value(1));
    send(first, textJson("Второе A")).andExpect(jsonPath("$.number").value(2));

    mvc.perform(get("/api/sessions/{id}/messages/{number}", second, 1))
      .andExpect(jsonPath("$.text").value("Сеанс B"));
    mvc.perform(get("/api/sessions/{id}/messages/{number}", second, 2))
      .andExpect(status().isNotFound())
      .andExpect(jsonPath("$.code").value("MESSAGE_NOT_FOUND"));
  }

  @Test
  void closedSessionIsUnavailableAndRepeatedLogoutIsIdempotent() throws Exception {
    String id = createSession();
    send(id, textJson("Привет")).andExpect(status().isCreated());

    mvc.perform(delete("/api/sessions/{id}", id)).andExpect(status().isNoContent());
    mvc.perform(delete("/api/sessions/{id}", id)).andExpect(status().isNoContent());

    send(id, textJson("После выхода"))
      .andExpect(status().isNotFound())
      .andExpect(jsonPath("$.code").value("SESSION_NOT_FOUND"));
    mvc.perform(get("/api/sessions/{id}/messages/{number}", id, 1))
      .andExpect(status().isNotFound());
  }

  private String createSession() throws Exception {
    String body = mvc.perform(post("/api/sessions"))
      .andExpect(status().isCreated())
      .andReturn().getResponse().getContentAsString();
    return objectMapper.readTree(body).get("sessionId").asString();
  }

  private ResultActions send(String sessionId, String json) throws Exception {
    return mvc.perform(post("/api/sessions/{id}/messages", sessionId)
                         .contentType(MediaType.APPLICATION_JSON)
                         .content(json));
  }

  private static String textJson(String text) {
    return "{\"text\":\"" + text + "\"}";
  }
}