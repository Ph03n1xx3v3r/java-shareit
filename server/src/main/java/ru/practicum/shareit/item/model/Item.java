package ru.practicum.shareit.item.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import ru.practicum.shareit.request.model.Request;
import ru.practicum.shareit.user.model.User;

@Getter
@Setter
@ToString
@Entity
@Table(name = "items", schema = "public")
public class Item {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id; // уникальный идентификатор вещи;

    @Column(name = "name", length = 200, nullable = false)
    private String name; // краткое название

    @Column(name = "description", length = 200)
    private String description; // развёрнутое описание

    @Column(name = "is_available", nullable = false)
    private Boolean available; // статус о том, доступна или нет вещь для аренды

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "owner_id") // id владельца вещи
    private User user;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "request_id")
    private Request request; // если вещь была создана по запросу другого пользователя, то в этом поле будет храниться ссылка на соответствующий запрос
}
