package ru.practicum.shareit.booking;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.model.State;

@RestController
@RequestMapping(path = "/bookings")
@Validated
@Slf4j
public class BookingController {

    private final BookingClient bookingClient;

    public BookingController(BookingClient bookingClient) {
        this.bookingClient = bookingClient;
    }

    @PostMapping
    public ResponseEntity<Object> createBooking(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                @Valid @RequestBody BookingDto bookingDto) {
        log.info("Creating booking {}, userId={}", bookingDto, userId);
        return bookingClient.createBooking(bookingDto, userId);
    }

    @PatchMapping("/{bookingId}")
    public ResponseEntity<Object> approveBooking(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                 @PathVariable("bookingId") Long bookingId,
                                                 @RequestParam("approved") Boolean statusBooking) {
        return bookingClient.approveBooking(userId, bookingId, statusBooking);
    }

    @GetMapping("/{bookingId}")
    public ResponseEntity<Object> getBookingById(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                 @PathVariable("bookingId") Long bookingId) {
        return bookingClient.getBookingById(userId, bookingId);
    }

    @GetMapping()
    public ResponseEntity<Object> getAllBookingByUser(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                      @RequestParam(value = "state", defaultValue = "ALL", required = false)
                                                      State state) {

        return bookingClient.getAllBookingByUser(userId, state);
    }

    @GetMapping("/owner")
    public ResponseEntity<Object> getAllBookingByItemOwner(@RequestHeader("X-Sharer-User-Id") Long ownerId,
                                                           @RequestParam(value = "state", defaultValue = "ALL", required = false)
                                                           State state) {
        return bookingClient.getAllBookingByItemOwner(ownerId, state);
    }
}
