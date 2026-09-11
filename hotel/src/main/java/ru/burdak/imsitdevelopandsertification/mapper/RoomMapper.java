package ru.burdak.imsitdevelopandsertification.mapper;

import lombok.experimental.UtilityClass;
import ru.burdak.imsitdevelopandsertification.dto.RoomDto;
import ru.burdak.imsitdevelopandsertification.model.Room;

@UtilityClass
public class RoomMapper {
    public RoomDto toDto(Room room) {
        return new RoomDto(room.getNumber(),
                room.getPrice(),
                room.getCategory());
    }




}
