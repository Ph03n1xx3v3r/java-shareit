package ru.practicum.shareit.unitTest;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.item.dto.CommentDto;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;


@JsonTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class CommentDtoTest {
    private final JacksonTester<CommentDto> json;
    private CommentDto commentDto;
    private LocalDateTime created;

    @Test
    void itemRequestDto() throws Exception {
        created = LocalDateTime.now();

        commentDto = new CommentDto(
                1L,
                "cool TV",
                "Jhon",
                created
        );

        JsonContent<CommentDto> result = json.write(commentDto);
        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.text").isEqualTo("cool TV");
        assertThat(result).extractingJsonPathStringValue("$.authorName").isEqualTo("Jhon");
    }
}
