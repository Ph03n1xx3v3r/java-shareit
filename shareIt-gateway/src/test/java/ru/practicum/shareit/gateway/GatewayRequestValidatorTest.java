package ru.practicum.shareit.gateway;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GatewayRequestValidatorTest {
    private final GatewayRequestValidator validator = new GatewayRequestValidator();

    @Test
    void rejectsMissingUserHeaderForProtectedEndpoint() {
        Optional<String> error = validator.validate("POST", "/requests", null,
                bytes("{\"description\":\"Need a drill\"}"));
        assertTrue(error.isPresent());
    }

    @Test
    void rejectsMalformedJson() {
        Optional<String> error = validator.validate("POST", "/requests", "1", bytes("{oops"));
        assertEquals("Тело запроса должно быть корректным JSON", error.orElseThrow());
    }

    @Test
    void acceptsRequestIdAsOptionalField() {
        Optional<String> error = validator.validate("POST", "/items", "1",
                bytes("{\"name\":\"Drill\",\"description\":\"Power drill\",\"available\":true}"));
        assertTrue(error.isEmpty());
    }

    @Test
    void rejectsInvalidRequestId() {
        Optional<String> error = validator.validate("POST", "/items", "1",
                bytes("{\"name\":\"Drill\",\"description\":\"Power drill\",\"available\":true,\"requestId\":-1}"));
        assertTrue(error.isPresent());
    }

    @Test
    void rejectsBlankRequestDescription() {
        Optional<String> error = validator.validate("POST", "/requests", "1",
                bytes("{\"description\":\"   \"}"));
        assertEquals("Описание запроса не может быть пустым", error.orElseThrow());
    }

    @Test
    void acceptsValidBookingDates() {
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = start.plusDays(1);
        String json = "{\"itemId\":1,\"start\":\"" + start + "\",\"end\":\"" + end + "\"}";
        assertTrue(validator.validate("POST", "/bookings", "1", bytes(json)).isEmpty());
    }

    private byte[] bytes(String value) {
        return value.getBytes(StandardCharsets.UTF_8);
    }
}
