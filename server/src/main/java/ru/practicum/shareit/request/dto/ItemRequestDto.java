package ru.practicum.shareit.request.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ItemRequestDto {
    Long id; // уникальный идентификатор запроса
    @NotBlank
    @Size(max = 200)
    String description; // текст запроса, содержащий описание требуемой вещи
    LocalDateTime created; // дата и время создания запроса
}