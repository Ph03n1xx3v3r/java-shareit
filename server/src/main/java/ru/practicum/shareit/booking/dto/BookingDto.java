package ru.practicum.shareit.booking.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@Data
@AllArgsConstructor
@NoArgsConstructor
public class BookingDto {
    @NotNull
    private LocalDateTime start; // дата и время начала бронирования
    @NotNull
    private LocalDateTime end; // дата и время конца бронирования
    @NotNull
    private Long itemId; // вещь, которую пользователь бронирует
}