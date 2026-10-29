package ru.practicum.shareit.gateway;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class GatewayErrorHandler {
    @ExceptionHandler(GatewayValidationException.class)
    public ResponseEntity<Map<String, String>> handleValidation(GatewayValidationException exception) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", exception.getMessage()));
    }
}
