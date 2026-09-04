package ru.burdak.imsitdevelopandsertification.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.burdak.imsitdevelopandsertification.dto.RoomDto;
import ru.burdak.imsitdevelopandsertification.model.Client;
import ru.burdak.imsitdevelopandsertification.service.HotelService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/bookings")
public class HotelController {
    private final HotelService hotelService;

    @ResponseStatus(HttpStatus.OK)
    @GetMapping
    public List<RoomDto> getAvailableRoom(@RequestParam(value = "money") int money) {
        Client client = new Client();
        client.setMoney(money);
        log.info("Получен запрос: money: {}", money);
        return hotelService.getAvailableRoom(client);
    }
}
