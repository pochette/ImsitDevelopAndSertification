package ru.burdak.imsitdevelopandsertification.mapper;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import ru.burdak.imsitdevelopandsertification.model.*;
import ru.burdak.imsitdevelopandsertification.dto.RoomDto;
import static org.junit.jupiter.api.Assertions.*;
class RoomMapperTest {
    @ParameterizedTest
    @EnumSource(RoomCategory.class)
    void preservesRoomFields(RoomCategory category) {
        Room room = new Room();
        room.setNumber(25);
        room.setPrice(600);
        room.setCategory(category);
        room.setOccupied(true);
        assertEquals(new RoomDto(25, 600, category), RoomMapper.toDto(room));
        assertTrue(room.isOccupied());
    }
}
