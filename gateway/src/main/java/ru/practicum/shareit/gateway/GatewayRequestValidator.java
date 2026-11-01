package ru.practicum.shareit.gateway;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.regex.Pattern;

/** Validates user input before the request reaches the main server. */
@Component
public class GatewayRequestValidator {
    private static final String USER_HEADER = "X-Sharer-User-Id";
    private static final Pattern EMAIL = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");
    private final ObjectMapper objectMapper = new ObjectMapper();

    public Optional<String> validate(String method, String path, String userId, byte[] body) {
        String normalizedMethod = method == null ? "" : method.toUpperCase();
        String normalizedPath = path == null ? "" : path;

        if (requiresUserHeader(normalizedMethod, normalizedPath)) {
            if (userId == null || userId.isBlank()) {
                return Optional.of("Необходимо передать заголовок " + USER_HEADER);
            }
            try {
                if (Long.parseLong(userId) <= 0) {
                    return Optional.of("Заголовок " + USER_HEADER + " должен содержать положительный id");
                }
            } catch (NumberFormatException exception) {
                return Optional.of("Заголовок " + USER_HEADER + " должен содержать число");
            }
        }

        if (body == null || body.length == 0 || !normalizedMethod.equals("POST")
                && !normalizedMethod.equals("PATCH")) {
            return Optional.empty();
        }

        JsonNode json;
        try {
            json = objectMapper.readTree(body);
        } catch (IOException exception) {
            return Optional.of("Тело запроса должно быть корректным JSON");
        }
        if (json == null || !json.isObject()) {
            return Optional.of("Тело запроса должно быть JSON-объектом");
        }

        if (normalizedPath.equals("/users") && normalizedMethod.equals("POST")) {
            return validateUser(json, true);
        }
        if (normalizedPath.matches("/users/\\d+") && normalizedMethod.equals("PATCH")) {
            return validateUser(json, false);
        }
        if (normalizedPath.equals("/items") && normalizedMethod.equals("POST")) {
            return validateItem(json, true);
        }
        if (normalizedPath.matches("/items/\\d+") && normalizedMethod.equals("PATCH")) {
            return validateItem(json, false);
        }
        if (normalizedPath.equals("/requests") && normalizedMethod.equals("POST")) {
            Optional<String> error = requiredText(json, "description", "Описание запроса не может быть пустым");
            if (error.isPresent()) return error;
            if (json.get("description").asText().length() > 1000) {
                return Optional.of("Описание запроса не должно превышать 1000 символов");
            }
            return Optional.empty();
        }
        if (normalizedPath.equals("/bookings") && normalizedMethod.equals("POST")) {
            return validateBooking(json);
        }
        if (normalizedPath.matches("/items/\\d+/comment") && normalizedMethod.equals("POST")) {
            return requiredText(json, "text", "Текст комментария не может быть пустым");
        }
        return Optional.empty();
    }

    private boolean requiresUserHeader(String method, String path) {
        if (path.equals("/requests") && (method.equals("POST") || method.equals("GET"))) return true;
        if (path.equals("/requests/all")) return method.equals("GET");
        if (path.equals("/items") && (method.equals("POST") || method.equals("GET"))) return true;
        if (path.matches("/items/\\d+") && method.equals("PATCH")) return true;
        if (path.matches("/items/\\d+/comment") && method.equals("POST")) return true;
        if (path.startsWith("/bookings")) return true;
        return path.matches("/users/\\d+/.*") && method.equals("POST");
    }

    private Optional<String> validateUser(JsonNode json, boolean creating) {
        if (creating || json.has("name")) {
            Optional<String> nameError = requiredText(json, "name", "Имя не может быть пустым");
            if (nameError.isPresent()) return nameError;
        }
        if (creating || json.has("email")) {
            JsonNode email = json.get("email");
            if (email == null || !email.isTextual() || email.asText().isBlank()
                    || !EMAIL.matcher(email.asText()).matches()) {
                return Optional.of("Необходимо указать корректный email");
            }
        }
        return Optional.empty();
    }

    private Optional<String> validateItem(JsonNode json, boolean creating) {
        if (creating || json.has("name")) {
            Optional<String> error = requiredText(json, "name", "Название вещи не может быть пустым");
            if (error.isPresent()) return error;
        }
        if (creating || json.has("description")) {
            Optional<String> error = requiredText(json, "description", "Описание вещи не может быть пустым");
            if (error.isPresent()) return error;
        }
        if (creating || json.has("available")) {
            JsonNode available = json.get("available");
            if (available == null || !available.isBoolean()) {
                return Optional.of("Необходимо указать доступность вещи (true/false)");
            }
        }
        JsonNode requestId = json.get("requestId");
        if (requestId != null && !requestId.isNull()
                && (!requestId.canConvertToLong() || requestId.longValue() <= 0)) {
            return Optional.of("requestId должен быть положительным числом");
        }
        return Optional.empty();
    }

    private Optional<String> validateBooking(JsonNode json) {
        JsonNode itemId = json.get("itemId");
        if (itemId == null || !itemId.canConvertToLong() || itemId.longValue() <= 0) {
            return Optional.of("Необходимо указать положительный itemId");
        }
        LocalDateTime start;
        LocalDateTime end;
        try {
            start = LocalDateTime.parse(text(json, "start"));
            end = LocalDateTime.parse(text(json, "end"));
        } catch (Exception exception) {
            return Optional.of("Даты start и end должны быть в формате ISO-8601");
        }
        LocalDateTime now = LocalDateTime.now();
        if (start.isBefore(now)) return Optional.of("Дата начала должна быть в настоящем или будущем");
        if (!end.isAfter(now)) return Optional.of("Дата окончания должна быть в будущем");
        if (!end.isAfter(start)) return Optional.of("Дата окончания должна быть позже даты начала");
        return Optional.empty();
    }

    private Optional<String> requiredText(JsonNode json, String field, String message) {
        JsonNode value = json.get(field);
        if (value == null || !value.isTextual() || value.asText().isBlank()) {
            return Optional.of(message);
        }
        return Optional.empty();
    }

    private String text(JsonNode json, String field) {
        JsonNode value = json.get(field);
        if (value == null || !value.isTextual() || value.asText().isBlank()) {
            throw new IllegalArgumentException("Missing field " + field);
        }
        return value.asText();
    }
}
