package ru.practicum.shareit.booking.dto;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class NewBookingDtoJsonTest {
    @Autowired
    private JacksonTester<NewBookingDto> json;

    @Test
    void serializesAndDeserializesDatesInIsoFormat() throws Exception {
        LocalDateTime start = LocalDateTime.of(2030, 1, 2, 10, 15);
        LocalDateTime end = LocalDateTime.of(2030, 1, 3, 10, 15);
        NewBookingDto dto = NewBookingDto.builder().itemId(7L).start(start).end(end).build();

        assertThat(json.write(dto)).extractingJsonPathStringValue("$.start").isEqualTo("2030-01-02T10:15:00");
        assertThat(json.write(dto)).extractingJsonPathStringValue("$.end").isEqualTo("2030-01-03T10:15:00");
        assertThat(json.parseObject(json.write(dto).getJson())).isEqualTo(dto);
    }
}
