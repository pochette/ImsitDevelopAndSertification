package ru.burdak.imsitdevelopandsertification.repository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.burdak.imsitdevelopandsertification.model.Room;
import ru.burdak.imsitdevelopandsertification.model.RoomCategory;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class RoomRepositoryTest {
    @Autowired RoomRepository repository;

    @ParameterizedTest
    @CsvSource({"199,0", "200,10", "399,10", "400,20", "599,20", "600,25"})
    void filtersByBudgetAndSorts(int money, int count) {
        var rooms = repository.findByPriceLessThanEqualAndOccupiedFalseOrderByPriceAsc(money);
        assertEquals(count, rooms.size());
        assertTrue(rooms.stream().allMatch(room -> room.getPrice() <= money && !room.isOccupied()));
        assertEquals(rooms.stream().map(Room::getPrice).sorted().toList(),
                rooms.stream().map(Room::getPrice).toList());
    }

    @Test
    void seedHasRequiredCategories() {
        var rooms = repository.findAll();
        assertEquals(25, rooms.size());
        assertEquals(10, rooms.stream().filter(r -> r.getPrice() == 200 && r.getCategory() == RoomCategory.ECONOMY).count());
        assertEquals(10, rooms.stream().filter(r -> r.getPrice() == 400 && r.getCategory() == RoomCategory.STANDARD).count());
        assertEquals(5, rooms.stream().filter(r -> r.getPrice() == 600 && r.getCategory() == RoomCategory.PREMIUM).count());
    }

    @Test
    void excludesOccupiedRooms() {
        Room occupied = repository.findAll().get(0);
        occupied.setOccupied(true);
        repository.saveAndFlush(occupied);
        var rooms = repository.findByPriceLessThanEqualAndOccupiedFalseOrderByPriceAsc(600);
        assertEquals(24, rooms.size());
        assertTrue(rooms.stream().noneMatch(r -> r.getId().equals(occupied.getId())));
    }

    @Test
    void returnsEmptyWhenAllAffordableRoomsOccupied() {
        var cheap = repository.findByPriceLessThanEqualAndOccupiedFalseOrderByPriceAsc(200);
        cheap.forEach(room -> room.setOccupied(true));
        repository.saveAllAndFlush(cheap);
        assertTrue(repository.findByPriceLessThanEqualAndOccupiedFalseOrderByPriceAsc(200).isEmpty());
        assertEquals(15, repository.findByPriceLessThanEqualAndOccupiedFalseOrderByPriceAsc(600).size());
    }
}
