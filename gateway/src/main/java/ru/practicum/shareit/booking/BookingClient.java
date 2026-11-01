package ru.practicum.shareit.booking;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.model.State;
import ru.practicum.shareit.client.BaseClient;
import ru.practicum.shareit.exception.ConditionsNotMetException;

import java.time.LocalDateTime;
import java.util.Map;

@Slf4j
@Service
public class BookingClient extends BaseClient {
    private static final String API_PREFIX = "/bookings";

    @Autowired
    public BookingClient(@Value("${shareit-server.url}") String serverUrl, RestTemplateBuilder builder) {
        super(
                builder
                        .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl + API_PREFIX))
                        .requestFactory(() -> new HttpComponentsClientHttpRequestFactory())
                        .build()
        );
    }

    public ResponseEntity<Object> getBookingById(long userId, Long bookingId) {
        return get("/" + bookingId, userId);
    }

    public ResponseEntity<Object> createBooking(BookingDto bookingDto, Long userId) {
        checkDateTimeItem(bookingDto);
        return post("", userId, bookingDto);
    }

    public ResponseEntity<Object> approveBooking(Long userId, Long bookingId, Boolean statusBooking) {
        return patch("/" + bookingId + "?approved=" + statusBooking, userId, null);
    }

    public ResponseEntity<Object> getAllBookingByUser(Long userId, State state) {
        Map<String, Object> parameters = Map.of(
                "state", state
        );
        return get("?state={state}", userId, parameters);
    }

    public ResponseEntity<Object> getAllBookingByItemOwner(Long ownerId, State state) {
        Map<String, Object> parameters = Map.of(
                "state", state
        );
        return get("/owner?state={state}", ownerId, parameters);
    }

    private void checkDateTimeItem(BookingDto bookingDto) {
        LocalDateTime now = LocalDateTime.now();
        if (bookingDto.getEnd().isBefore(now) ||
                bookingDto.getStart().isAfter(bookingDto.getEnd())) {
            log.warn("Не корректные даты периода бронирования");
            throw new ConditionsNotMetException("Не корректные даты периода бронирования");
        }
    }
}
