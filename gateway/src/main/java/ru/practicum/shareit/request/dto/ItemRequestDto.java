package ru.practicum.shareit.request.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@Data
public class ItemRequestDto {
    Long id; // уникальный идентификатор запроса
    @NotBlank
    @Size(max = 200)
    String description; // текст запроса, содержащий описание требуемой вещи
    LocalDateTime created; // дата и время создания запроса
}