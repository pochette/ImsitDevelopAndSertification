package ru.burdak.imsitdevelopandsertification.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.burdak.imsitdevelopandsertification.dto.RoomDto;
import ru.burdak.imsitdevelopandsertification.model.Client;
import ru.burdak.imsitdevelopandsertification.service.HotelService;

import java.util.List;

@Tag(name = "Поиск свободных номеров")
@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/bookings")
public class HotelController {
    private final HotelService hotelService;

    @Operation(summary = "Найти свободные номера по бюджету", description = "Возвращает свободные номера с ценой <= money по возрастанию цены. Не занимает номер, не сохраняет клиента и не списывает деньги. Порядок номеров с одинаковой ценой не фиксирован.")
    @ApiResponse(responseCode = "200", description = "Подходящие номера")
    @ApiResponse(responseCode = "400", description = "Бюджет отсутствует или не является целым числом int", content = @Content)
    @ApiResponse(responseCode = "404", description = "Подходящих номеров нет", content = @Content(mediaType = "application/json", schema = @Schema(type = "object"), examples = @ExampleObject(value = "{\"message\":\"Таких номеров нет\"}")))
    @ResponseStatus(HttpStatus.OK)
    @GetMapping
    public List<RoomDto> getAvailableRoom(@Parameter(description = "Бюджет в целых единицах. При нуле и отрицательном значении подходящих номеров нет (404).", example = "400", required = true) @RequestParam(value = "money") int money) {
        Client client = new Client();
        client.setMoney(money);
        log.info("Получен запрос: money: {}", money);
        return hotelService.getAvailableRoom(client);
    }
}
