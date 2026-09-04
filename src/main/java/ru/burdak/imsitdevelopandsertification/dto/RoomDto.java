package ru.burdak.imsitdevelopandsertification.dto;

import ru.burdak.imsitdevelopandsertification.model.RoomCategory;

public record RoomDto(
        int number,
        int price,
        RoomCategory roomCategory) {}
