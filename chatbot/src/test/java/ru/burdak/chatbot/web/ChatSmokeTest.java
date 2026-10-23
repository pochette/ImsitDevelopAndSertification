package ru.burdak.chatbot.web;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Smoke Test: пригодность сборки для дальнейшего тестирования (Acceptable / Unacceptable).
 * Запуск только smoke-тестов: mvnw -pl chatbot test -Dgroups=smoke
 */
@Tag("smoke")
@SpringBootTest(properties = "chatbot.cleanup-interval=3600000")
@AutoConfigureMockMvc
class ChatSmokeTest {
  @Autowired private MockMvc mvc;
  @Autowired private ObjectMapper objectMapper;

  @Test
  void buildIsAcceptableForFurtherTesting() throws Exception {
    String body = mvc.perform(post("/api/sessions"))
      .andExpect(status().isCreated())
      .andExpect(jsonPath("$.sessionId").exists())
      .andReturn().getResponse().getContentAsString();
    String id = objectMapper.readTree(body).get("sessionId").asString();

    mvc.perform(post("/api/sessions/{id}/messages", id)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content("{\"text\":\"Привет\"}"))
      .andExpect(status().isCreated())
      .andExpect(jsonPath("$.number").value(1));

    mvc.perform(get("/api/sessions/{id}/messages/{number}", id, 1))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.text").value("Привет"));

    mvc.perform(delete("/api/sessions/{id}", id))
      .andExpect(status().isNoContent());
  }
}