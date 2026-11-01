package ru.practicum.shareit.booking.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@Data
public class BookingDto {
    @NotNull
    @FutureOrPresent
    private LocalDateTime start; // дата и время начала бронирования
    @NotNull
    @Future
    private LocalDateTime end; // дата и время конца бронирования
    private Long itemId; // вещь, которую пользователь бронирует
}