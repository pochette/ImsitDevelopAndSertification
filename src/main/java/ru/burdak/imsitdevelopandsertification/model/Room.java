package ru.burdak.imsitdevelopandsertification.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "rooms")
@Getter
@Setter
@RequiredArgsConstructor

public class Room {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int number;
    private int price;
    private boolean occupied;

    @Enumerated(value = EnumType.STRING)
    private RoomCategory category;


}
