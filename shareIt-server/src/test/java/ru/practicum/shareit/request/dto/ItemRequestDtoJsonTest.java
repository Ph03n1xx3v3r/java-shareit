package ru.practicum.shareit.request.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class ItemRequestDtoJsonTest {
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void serializesCreatedDateAndItemAnswers() throws Exception {
        ItemRequestDto dto = ItemRequestDto.builder()
                .id(5L)
                .description("Нужен шуруповёрт")
                .created(LocalDateTime.of(2026, 10, 29, 10, 15, 30))
                .items(List.of(ItemRequestAnswerDto.builder()
                        .id(12L)
                        .name("Шуруповёрт")
                        .ownerId(3L)
                        .build()))
                .build();

        String json = objectMapper.writeValueAsString(dto);

        assertThat(json).contains("\"created\":\"2026-10-29T10:15:30\"");
        assertThat(json).contains("\"ownerId\":3");
        ItemRequestDto restored = objectMapper.readValue(json, ItemRequestDto.class);
        assertThat(restored.getCreated()).isEqualTo(dto.getCreated());
        assertThat(restored.getItems()).hasSize(1);
        assertThat(restored.getItems().get(0).getOwnerId()).isEqualTo(3L);
    }
}
