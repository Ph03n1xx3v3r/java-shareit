package ru.practicum.shareit.item.dto;

import lombok.Data;
import org.springframework.stereotype.Component;

@Component
@Data
public class ItemForRequestDto {
    Long id;
    String name; // краткое название
    Long ownerId; // id владельца
}