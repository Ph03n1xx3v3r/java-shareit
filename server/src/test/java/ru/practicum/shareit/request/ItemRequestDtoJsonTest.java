package ru.practicum.shareit.request;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import ru.practicum.shareit.request.dto.ItemRequestAnswerDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class ItemRequestDtoJsonTest {
    @Autowired
    private JacksonTester<ItemRequestDto> json;

    @Test
    void serializesRequestAndAnswerItems() throws Exception {
        ItemRequestDto dto = ItemRequestDto.builder()
                .id(7L)
                .description("Need a drill")
                .created(LocalDateTime.of(2026, 10, 20, 12, 30))
                .items(List.of(ItemRequestAnswerDto.builder().id(3L).name("Drill").ownerId(2L).build()))
                .build();

        assertThat(json.write(dto)).extractingJsonPathNumberValue("$.id").isEqualTo(7);
        assertThat(json.write(dto)).extractingJsonPathStringValue("$.description").isEqualTo("Need a drill");
        assertThat(json.write(dto)).extractingJsonPathNumberValue("$.items[0].ownerId").isEqualTo(2);
        assertThat(json.write(dto)).extractingJsonPathStringValue("$.items[0].name").isEqualTo("Drill");
    }
}
