package ru.burdak.chatbot.documentation;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasKey;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class OpenApiIntegrationTest {
    @Autowired private MockMvc mvc;

    @Test
    void publishesApiContractAndSwaggerResources() throws Exception {
        mvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.info.title").value("Chatbot API"))
                .andExpect(jsonPath("$.paths").value(hasKey("/api/sessions")))
                .andExpect(jsonPath("$.paths['/api/sessions'].post.responses['201']").exists())
                .andExpect(jsonPath("$.paths['/api/sessions/{sessionId}'].get.responses['200']").exists())
                .andExpect(jsonPath("$.paths['/api/sessions/{sessionId}'].delete.responses['204']").exists())
                .andExpect(jsonPath("$.paths['/api/sessions/{sessionId}/messages'].post.responses['409']").exists())
                .andExpect(jsonPath("$.paths['/api/sessions/{sessionId}/messages/{number}'].get.responses['404']").exists())
                .andExpect(jsonPath("$.components.schemas.SendMessageRequest.properties.text.example").value("Привет, бот!"));
        mvc.perform(get("/swagger-ui.html"))
                .andExpect(status().is3xxRedirection())
                .andExpect(header().string("Location", containsString("swagger-ui/index.html")));
        mvc.perform(get("/swagger-ui/index.html"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Swagger UI")));
        mvc.perform(get("/v3/api-docs/swagger-config"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.url").value("/v3/api-docs"));
    }
}
