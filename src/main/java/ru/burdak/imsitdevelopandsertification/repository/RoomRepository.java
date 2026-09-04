package ru.burdak.imsitdevelopandsertification.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import ru.burdak.imsitdevelopandsertification.model.Room;

import java.util.List;

public interface RoomRepository extends JpaRepository<Room, Long> {


    List<Room> findByPriceLessThanEqualAndOccupiedFalseOrderByPriceAsc(int money);
}
