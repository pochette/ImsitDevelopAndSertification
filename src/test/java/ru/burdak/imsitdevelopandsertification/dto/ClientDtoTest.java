package ru.burdak.imsitdevelopandsertification.dto;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ClientDtoTest {
    @ParameterizedTest
    @ValueSource(ints = {Integer.MIN_VALUE, 0, 199, 200, 400, 600, Integer.MAX_VALUE})
    void preservesBudgetWithoutRoundingOrClamping(int money) {
        assertEquals(money, new ClientDto(money).money());
    }
}
