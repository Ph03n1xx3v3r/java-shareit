package ru.practicum.shareit.item.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ItemDto {
    Long id;
    @NotBlank
    @Size(max = 200)
    String name; // краткое название
    @Size(max = 200)
    String description; // развёрнутое описание
    @NotNull
    Boolean available; // статус о том, доступна или нет вещь для аренды
    Long requestId;
}