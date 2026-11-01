package ru.practicum.shareit.booking.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.booking.model.StatusBooking;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;

@Component
@Data
@AllArgsConstructor
@NoArgsConstructor
public class BookingSpecificationDto {

    private Long id;

    private LocalDateTime start; // дата и время начала бронирования

    private LocalDateTime end; // дата и время конца бронирования

    private Item item; // вещь, которую пользователь бронирует

    private User booker; // пользователь, который осуществляет бронирование

    private StatusBooking status; // статус бронирования
}
