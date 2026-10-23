package ru.burdak.imsitdevelopandsertification.integration;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Smoke Test: пригодность сборки для дальнейшего тестирования (Acceptable / Unacceptable).
 * Запуск только smoke-тестов: mvnw -pl hotel test -Dgroups=smoke
 */
@Tag("smoke")
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class HotelSmokeTest {
  @Autowired private MockMvc mvc;

  @Test
  void buildIsAcceptableForFurtherTesting() throws Exception {
    mvc.perform(get("/bookings").param("money", "400"))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.length()").value(20));

    mvc.perform(get("/bookings").param("money", "100"))
      .andExpect(status().isNotFound())
      .andExpect(jsonPath("$.message").value("Таких номеров нет"));
  }
}