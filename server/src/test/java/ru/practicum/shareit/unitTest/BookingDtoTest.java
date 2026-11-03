package ru.practicum.shareit.unitTest;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.booking.dto.BookingDto;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;


@JsonTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class BookingDtoTest {
    private final JacksonTester<BookingDto> json;
    private BookingDto bookingDto;
    private LocalDateTime start;
    private LocalDateTime end;

    @Test
    void testUserDto() throws Exception {
        start = LocalDateTime.now();
        end = LocalDateTime.now().plusDays(5);
        bookingDto = new BookingDto(
                start,
                end,
                1L
        );

        JsonContent<BookingDto> result = json.write(bookingDto);
        System.out.println(result);
        assertThat(result).extractingJsonPathNumberValue("$.itemId").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.start")
                .matches("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}(\\.\\d+)?");
        assertThat(result).extractingJsonPathStringValue("$.end")
                .matches("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}(\\.\\d+)?");
    }

}
