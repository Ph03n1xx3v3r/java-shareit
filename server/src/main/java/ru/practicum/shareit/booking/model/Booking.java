package ru.practicum.shareit.booking.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@Entity
@Table(name = "bookings", schema = "public")
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id; // уникальный идентификатор бронирования

    @Column(name = "start_date", nullable = false)
    private LocalDateTime start; // дата и время начала бронирования

    @Column(name = "end_date", nullable = false)
    private LocalDateTime end; // дата и время конца бронирования

    @ManyToOne
    @JoinColumn(name = "item_id", nullable = false) // вещь, которую пользователь бронирует
    private Item item;

    @ManyToOne
    @JoinColumn(name = "booker_id", nullable = false)  // пользователь, который осуществляет бронирование
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private StatusBooking status; // статус бронирования

}