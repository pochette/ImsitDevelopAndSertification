package ru.burdak.imsitdevelopandsertification.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.burdak.imsitdevelopandsertification.dto.RoomDto;
import ru.burdak.imsitdevelopandsertification.exception.NotFoundException;
import ru.burdak.imsitdevelopandsertification.mapper.RoomMapper;
import ru.burdak.imsitdevelopandsertification.model.Client;
import ru.burdak.imsitdevelopandsertification.model.Room;
import ru.burdak.imsitdevelopandsertification.repository.ClientRepository;
import ru.burdak.imsitdevelopandsertification.repository.RoomRepository;

import java.util.Collections;
import java.util.List;

@Transactional
@Service
@RequiredArgsConstructor
public class HotelService {
    private final RoomRepository roomRepository;
    private final ClientRepository clientRepository;

    @Transactional
    public List<RoomDto> getAvailableRoom(Client client) {
        List<Room> rooms = roomRepository.findByPriceLessThanEqualAndOccupiedFalseOrderByPriceAsc(client.getMoney());
        if (rooms.isEmpty()) {
            throw new NotFoundException("Таких номеров нет");
        }
        return rooms.stream()
                .map(RoomMapper::toDto)
                .toList();
    }
}
