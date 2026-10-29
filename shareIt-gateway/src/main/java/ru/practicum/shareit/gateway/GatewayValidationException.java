package ru.practicum.shareit.gateway;

public class GatewayValidationException extends RuntimeException {
    public GatewayValidationException(String message) {
        super(message);
    }
}
