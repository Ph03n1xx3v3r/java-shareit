package ru.practicum.shareit.booking.mapper;

import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingSpecificationDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.StatusBooking;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

@Component
@NoArgsConstructor
public class BookingMapper {
    public static Booking toBooking(BookingDto bookingDto, Item item, User user) {
        Booking booking = new Booking();
        booking.setUser(user);
        booking.setItem(item);
        booking.setEnd(bookingDto.getEnd());
        booking.setStart(bookingDto.getStart());
        booking.setStatus(StatusBooking.WAITING);
        return booking;
    }

    public static BookingDto toBookingDto(Booking booking) {
        BookingDto bookingDto = new BookingDto();
        bookingDto.setItemId(booking.getItem().getId());
        bookingDto.setEnd(booking.getEnd());
        bookingDto.setStart(booking.getStart());
        return bookingDto;
    }

    public static BookingSpecificationDto toBookingSpecificationDto(Booking booking) {
        BookingSpecificationDto bookingSpecificationDto = new BookingSpecificationDto();
        bookingSpecificationDto.setId(booking.getId());
        bookingSpecificationDto.setBooker(booking.getUser());
        bookingSpecificationDto.setEnd(booking.getEnd());
        bookingSpecificationDto.setStart(booking.getStart());
        bookingSpecificationDto.setStatus(booking.getStatus());
        bookingSpecificationDto.setItem(booking.getItem());
        return bookingSpecificationDto;
    }
}
