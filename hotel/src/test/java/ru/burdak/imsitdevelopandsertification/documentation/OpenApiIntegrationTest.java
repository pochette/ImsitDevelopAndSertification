package ru.burdak.imsitdevelopandsertification.documentation;

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
                .andExpect(jsonPath("$.info.title").value("Hotel API"))
                .andExpect(jsonPath("$.paths").value(hasKey("/bookings")))
                .andExpect(jsonPath("$.paths['/bookings'].get.responses['200']").exists())
                .andExpect(jsonPath("$.paths['/bookings'].get.responses['400']").exists())
                .andExpect(jsonPath("$.paths['/bookings'].get.responses['404'].content['application/json'].schema.type").value("object"))
                .andExpect(jsonPath("$.paths['/bookings'].get.parameters[0].name").value("money"))
                .andExpect(jsonPath("$.paths['/bookings'].get.parameters[0].required").value(true));
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
