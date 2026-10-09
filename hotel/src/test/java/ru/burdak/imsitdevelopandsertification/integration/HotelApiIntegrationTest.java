package ru.burdak.imsitdevelopandsertification.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class HotelApiIntegrationTest {
    @Autowired private MockMvc mvc;

    @Test
    void returnsAvailableRoomsForBudgetBoundaries() throws Exception {
        mvc.perform(get("/bookings").param("money", "200"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(10))
                .andExpect(jsonPath("$[0].price").value(200))
                .andExpect(jsonPath("$[0].roomCategory").value("ECONOMY"));

        mvc.perform(get("/bookings").param("money", "400"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(20))
                .andExpect(jsonPath("$[10].roomCategory").value("STANDARD"));

        mvc.perform(get("/bookings").param("money", "600"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(25))
                .andExpect(jsonPath("$[20].roomCategory").value("PREMIUM"));
    }

    @Test
    void returnsNotFoundWhenNoRoomFitsBudget() throws Exception {
        mvc.perform(get("/bookings").param("money", "199"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Таких номеров нет"));
    }

    @Test
    void rejectsInvalidRequests() throws Exception {
        mvc.perform(get("/bookings"))
                .andExpect(status().isBadRequest());

        mvc.perform(get("/bookings").param("money", "abc"))
                .andExpect(status().isBadRequest());

        mvc.perform(post("/bookings").param("money", "600"))
                .andExpect(status().isMethodNotAllowed());
    }
}
