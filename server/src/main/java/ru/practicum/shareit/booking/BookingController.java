package ru.practicum.shareit.booking;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingSpecificationDto;
import ru.practicum.shareit.booking.model.State;
import ru.practicum.shareit.booking.service.BookingService;

import java.util.List;

@RestController
@RequestMapping(path = "/bookings")
public class BookingController {

    BookingService bookingService;

    public BookingController(@Qualifier("bookingServiceImpl") BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping
    public BookingSpecificationDto createBooking(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                 @RequestBody BookingDto bookingDto) {
        return bookingService.createBooking(bookingDto, userId);
    }

    @PatchMapping("/{bookingId}")
    public BookingSpecificationDto approveBooking(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                  @PathVariable("bookingId") Long bookingId,
                                                  @RequestParam("approved") Boolean statusBooking) {
        return bookingService.approveBooking(userId, bookingId, statusBooking);
    }

    @GetMapping("/{bookingId}")
    public BookingSpecificationDto getBookingById(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                  @PathVariable("bookingId") Long bookingId) {
        return bookingService.getBookingById(bookingId, userId);
    }

    @GetMapping()
    public List<BookingSpecificationDto> getAllBookingByUser(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                             @RequestParam(value = "state", defaultValue = "ALL", required = false)
                                                             State state) {

        return bookingService.getAllBookingByUser(userId, state);
    }

    @GetMapping("/owner")
    public List<BookingSpecificationDto> getAllBookingByItemOwner(@RequestHeader("X-Sharer-User-Id") Long ownerId,
                                                                  @RequestParam(value = "state", defaultValue = "ALL", required = false)
                                                                  State state) {
        return bookingService.getAllBookingByItemOwner(ownerId, state);
    }


}
