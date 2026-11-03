package ru.practicum.shareit.item.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ItemListDto {
    Long id;
    String name; // краткое название
    String description; // развёрнутое описание
    LocalDateTime lastBooking;
    LocalDateTime nextBooking;
    List<CommentDto> comments;
}
