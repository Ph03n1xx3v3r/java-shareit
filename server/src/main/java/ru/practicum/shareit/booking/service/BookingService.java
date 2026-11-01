package ru.practicum.shareit.booking.service;

import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingSpecificationDto;
import ru.practicum.shareit.booking.model.State;

import java.util.List;

public interface BookingService {

    BookingSpecificationDto createBooking(BookingDto bookingDto, Long userId);

    BookingSpecificationDto approveBooking(Long userId, Long bookingId, Boolean statusBooking);

    BookingSpecificationDto getBookingById(Long bookingId, Long userId);

    List<BookingSpecificationDto> getAllBookingByUser(Long userId, State state);

    List<BookingSpecificationDto> getAllBookingByItemOwner(Long userId, State state);

}
