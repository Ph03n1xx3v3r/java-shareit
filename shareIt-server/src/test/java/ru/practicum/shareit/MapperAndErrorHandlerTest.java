package ru.practicum.shareit;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingMapper;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.ErrorHandler;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserMapper;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class MapperAndErrorHandlerTest {
    @Test
    void mapsUsersItemsAndBookings() {
        User user = User.builder().id(1L).name("Alex").email("a@example.com").build();
        assertThat(UserMapper.toUser(UserMapper.toUserDto(user)).getEmail()).isEqualTo(user.getEmail());
        ItemRequest request = ItemRequest.builder().id(4L).description("Need").build();
        Item item = Item.builder().id(2L).name("Drill").description("Tool").available(true)
                .owner(user).request(request).build();
        assertThat(ItemMapper.toItemDto(item).getRequestId()).isEqualTo(4L);
        assertThat(ItemMapper.toItem(ItemMapper.toItemDto(item)).getName()).isEqualTo("Drill");
        Booking booking = Booking.builder().id(3L).start(LocalDateTime.now()).end(LocalDateTime.now().plusDays(1))
                .item(item).booker(user).status(BookingStatus.WAITING).build();
        assertThat(BookingMapper.toDto(booking).getStatus()).isEqualTo(BookingStatus.WAITING);
        assertThat(BookingMapper.toShortDto(booking).getBookerId()).isEqualTo(1L);
        assertThat(BookingMapper.toShortDto(null)).isNull();
    }

    @Test
    void errorHandlerReturnsCorrectHttpStatuses() {
        ErrorHandler handler = new ErrorHandler();
        assertThat(handler.handleNotFound(new NotFoundException("missing")).getStatusCode())
                .isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(handler.handleConflict(new ConflictException("conflict")).getStatusCode())
                .isEqualTo(HttpStatus.CONFLICT);
        assertThat(handler.handleValidation(new ValidationException("invalid")).getStatusCode())
                .isEqualTo(HttpStatus.BAD_REQUEST);
    }
}
