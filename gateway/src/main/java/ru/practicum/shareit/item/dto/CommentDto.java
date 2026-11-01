package ru.practicum.shareit.item.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@Data
public class CommentDto {

    Long id;
    @NotBlank
    String text;
    String authorName;
    LocalDateTime created;
}
