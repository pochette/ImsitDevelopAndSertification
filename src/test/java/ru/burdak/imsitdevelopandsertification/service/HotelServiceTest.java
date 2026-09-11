package ru.burdak.imsitdevelopandsertification.service;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import ru.burdak.imsitdevelopandsertification.dto.RoomDto;
import ru.burdak.imsitdevelopandsertification.exception.NotFoundException;
import ru.burdak.imsitdevelopandsertification.model.*;
import ru.burdak.imsitdevelopandsertification.repository.*;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
class HotelServiceTest {
    private final RoomRepository rooms = mock(RoomRepository.class);
    private final ClientRepository clients = mock(ClientRepository.class);
    private final HotelService service = new HotelService(rooms, clients);
    @ParameterizedTest
    @ValueSource(ints = {200, 399, 400, 599, 600, Integer.MAX_VALUE})
    void passesBudgetAndMapsRooms(int money) {
        Client client = new Client();
        client.setMoney(money);
        Room first = room(1);
        Room second = room(2);
        when(rooms.findByPriceLessThanEqualAndOccupiedFalseOrderByPriceAsc(money)).thenReturn(List.of(first, second));
        assertEquals(List.of(new RoomDto(1, 200, RoomCategory.ECONOMY), new RoomDto(2, 200, RoomCategory.ECONOMY)), service.getAvailableRoom(client));
        verify(rooms).findByPriceLessThanEqualAndOccupiedFalseOrderByPriceAsc(money);
        verifyNoMoreInteractions(rooms);
        verifyNoInteractions(clients);
        assertFalse(first.isOccupied());
    }
    @ParameterizedTest
    @ValueSource(ints = {-1, 0, 199, 600})
    void reportsNoAffordableOrFreeRooms(int money) {
        Client client = new Client();
        client.setMoney(money);
        when(rooms.findByPriceLessThanEqualAndOccupiedFalseOrderByPriceAsc(money)).thenReturn(List.of());
        assertEquals("Таких номеров нет", assertThrows(NotFoundException.class, () -> service.getAvailableRoom(client)).getMessage());
        verifyNoInteractions(clients);
    }
    private Room room(int number) {
        Room room = new Room();
        room.setNumber(number);
        room.setPrice(200);
        room.setCategory(RoomCategory.ECONOMY);
        return room;
    }
}
