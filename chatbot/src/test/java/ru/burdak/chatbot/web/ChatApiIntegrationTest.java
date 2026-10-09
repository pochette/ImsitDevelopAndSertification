package ru.burdak.chatbot.web;

import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = "chatbot.cleanup-interval=3600000")
@AutoConfigureMockMvc
class ChatApiIntegrationTest {
    @Autowired private MockMvc mvc;
    @Autowired private ObjectMapper objectMapper;

    @Test
    void completesLoginRememberAndLogoutFlow() throws Exception {
        String response = mvc.perform(post("/api/sessions"))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", startsWith("http://localhost/api/sessions/")))
                .andExpect(jsonPath("$.state").value("ACTIVE"))
                .andExpect(jsonPath("$.messageCount").value(0))
                .andReturn().getResponse().getContentAsString();
        UUID id = UUID.fromString(objectMapper.readTree(response).get("sessionId").asText());

        mvc.perform(get("/api/sessions/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sessionId").value(id.toString()));

        mvc.perform(post("/api/sessions/{id}/messages", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"text\":\"Привет, бот 🌍\"}"))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", startsWith("http://localhost/api/sessions/" + id + "/messages/")))
                .andExpect(jsonPath("$.number").value(1))
                .andExpect(jsonPath("$.text").value("Привет, бот 🌍"));

        mvc.perform(get("/api/sessions/{id}/messages/{number}", id, 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.number").value(1))
                .andExpect(jsonPath("$.text").value("Привет, бот 🌍"));

        mvc.perform(delete("/api/sessions/{id}", id)).andExpect(status().isNoContent());
        mvc.perform(get("/api/sessions/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("SESSION_NOT_FOUND"));
    }

    @Test
    void returnsJsonErrorsForInvalidAndMissingMessages() throws Exception {
        String response = mvc.perform(post("/api/sessions")).andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        UUID id = UUID.fromString(objectMapper.readTree(response).get("sessionId").asText());

        mvc.perform(post("/api/sessions/{id}/messages", id)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"text\":\" \"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"));

        mvc.perform(get("/api/sessions/{id}/messages/{number}", id, 5))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("MESSAGE_NOT_FOUND"));

        mvc.perform(post("/api/sessions/{id}/messages", id)
                        .contentType(MediaType.TEXT_PLAIN).content("plain text"))
                .andExpect(status().isUnsupportedMediaType())
                .andExpect(jsonPath("$.code").value("UNSUPPORTED_MEDIA_TYPE"));
    }
}
