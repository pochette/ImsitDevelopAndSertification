package ru.burdak.imsitdevelopandsertification.controller;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import ru.burdak.imsitdevelopandsertification.dto.RoomDto;
import ru.burdak.imsitdevelopandsertification.exception.*;
import ru.burdak.imsitdevelopandsertification.model.RoomCategory;
import ru.burdak.imsitdevelopandsertification.service.HotelService;
import java.util.List;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
class HotelControllerTest {
    private HotelService service;
    private MockMvc mvc;
    @BeforeEach
    void setUp() {
        service = mock(HotelService.class);
        mvc = MockMvcBuilders.standaloneSetup(new HotelController(service)).setControllerAdvice(new ErrorHandler()).build();
    }
    @Test
    void returnsJsonAndPassesBudget() throws Exception {
        when(service.getAvailableRoom(any())).thenReturn(List.of(new RoomDto(11, 400, RoomCategory.STANDARD)));
        mvc.perform(get("/bookings").param("money", "400"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].number").value(11)).andExpect(jsonPath("$[0].price").value(400))
                .andExpect(jsonPath("$[0].roomCategory").value("STANDARD"));
        verify(service).getAvailableRoom(argThat(client -> client.getMoney() == 400));
    }
    @Test
    void returns404ForUnavailableRooms() throws Exception {
        when(service.getAvailableRoom(any())).thenThrow(new NotFoundException("Таких номеров нет"));
        mvc.perform(get("/bookings").param("money", "199"))
                .andExpect(status().isNotFound()).andExpect(jsonPath("$.message").value("Таких номеров нет"));
    }
    @ParameterizedTest
    @ValueSource(strings = {"abc", "1.5", "2147483648", ""})
    void rejectsInvalidBudget(String value) throws Exception {
        mvc.perform(get("/bookings").param("money", value)).andExpect(status().isBadRequest());
        verifyNoInteractions(service);
    }
    @Test
    void requiresBudget() throws Exception {
        mvc.perform(get("/bookings")).andExpect(status().isBadRequest());
        verifyNoInteractions(service);
    }
    @Test
    void rejectsUnsupportedMethod() throws Exception {
        mvc.perform(post("/bookings").param("money", "600")).andExpect(status().isMethodNotAllowed());
        verifyNoInteractions(service);
    }
}
