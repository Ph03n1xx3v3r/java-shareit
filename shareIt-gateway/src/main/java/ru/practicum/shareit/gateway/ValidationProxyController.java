package ru.practicum.shareit.gateway;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.URI;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Set;

@RestController
@RequestMapping({"/", "/**"})
public class ValidationProxyController {
    private static final Set<String> HOP_BY_HOP_HEADERS = Set.of(
            "host", "content-length", "connection", "transfer-encoding", "keep-alive", "upgrade"
    );

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final String serverUrl;

    public ValidationProxyController(RestTemplate restTemplate,
                                     ObjectMapper objectMapper,
                                     @Value("${shareit.server.url:http://localhost:9090}") String serverUrl) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        this.serverUrl = serverUrl;
    }

    @RequestMapping
    public ResponseEntity<byte[]> proxy(HttpServletRequest request,
                                        @RequestBody(required = false) byte[] body) throws IOException {
        String path = request.getRequestURI();
        validateRequest(request, body, path);

        String query = request.getQueryString();
        URI target = URI.create(serverUrl + path + (query == null ? "" : "?" + query));
        HttpHeaders headers = new HttpHeaders();
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames != null && headerNames.hasMoreElements()) {
            String name = headerNames.nextElement();
            if (!HOP_BY_HOP_HEADERS.contains(name.toLowerCase(Locale.ROOT))) {
                Enumeration<String> values = request.getHeaders(name);
                while (values.hasMoreElements()) {
                    headers.add(name, values.nextElement());
                }
            }
        }

        HttpMethod method = HttpMethod.valueOf(request.getMethod());
        ResponseEntity<byte[]> response = restTemplate.exchange(
                target, method, new HttpEntity<>(body, headers), byte[].class);
        HttpHeaders responseHeaders = new HttpHeaders();
        response.getHeaders().forEach((name, values) -> {
            if (!HOP_BY_HOP_HEADERS.contains(name.toLowerCase(Locale.ROOT))) {
                responseHeaders.put(name, values);
            }
        });
        return new ResponseEntity<>(response.getBody(), responseHeaders, response.getStatusCode());
    }

    private void validateRequest(HttpServletRequest request, byte[] body, String path) throws IOException {
        String method = request.getMethod();
        boolean hasBody = body != null && body.length > 0;
        if (!hasBody || !(method.equals("POST") || method.equals("PATCH"))) {
            validatePagination(request, path);
            return;
        }

        JsonNode json;
        try {
            json = objectMapper.readTree(body);
        } catch (IOException exception) {
            throw new GatewayValidationException("Тело запроса должно содержать корректный JSON");
        }
        if (json == null || !json.isObject()) {
            throw new GatewayValidationException("Тело запроса должно быть JSON-объектом");
        }

        if (path.equals("/users") && method.equals("POST")) {
            requiredText(json, "name", "Имя не может быть пустым", 255);
            String email = requiredText(json, "email", "Email не может быть пустым", 512);
            if (!email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
                throw new GatewayValidationException("Некорректный формат email");
            }
        } else if (path.matches("/users/\\d+") && method.equals("PATCH")) {
            optionalText(json, "name", "Имя не может быть пустым", 255);
            JsonNode emailNode = json.get("email");
            if (emailNode != null && !emailNode.isNull()) {
                String email = optionalText(json, "email", "Email не может быть пустым", 512);
                if (!email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
                    throw new GatewayValidationException("Некорректный формат email");
                }
            }
        } else if (path.equals("/items") && method.equals("POST")) {
            requiredText(json, "name", "Название вещи не может быть пустым", 255);
            requiredText(json, "description", "Описание вещи не может быть пустым", 1000);
            if (!json.hasNonNull("available") || !json.get("available").isBoolean()) {
                throw new GatewayValidationException("Необходимо указать доступность вещи");
            }
            JsonNode requestId = json.get("requestId");
            if (requestId != null && !requestId.isNull() && !requestId.canConvertToLong()) {
                throw new GatewayValidationException("requestId должен быть числом");
            }
        } else if (path.matches("/items/\\d+") && method.equals("PATCH")) {
            optionalText(json, "name", "Название вещи не может быть пустым", 255);
            optionalText(json, "description", "Описание вещи не может быть пустым", 1000);
            if (json.hasNonNull("available") && !json.get("available").isBoolean()) {
                throw new GatewayValidationException("available должен быть логическим значением");
            }
        } else if (path.matches("/items/\\d+/comment") && method.equals("POST")) {
            requiredText(json, "text", "Текст комментария не может быть пустым", 2000);
        } else if (path.equals("/requests") && method.equals("POST")) {
            requiredText(json, "description", "Описание запроса не может быть пустым", 1000);
        } else if (path.equals("/bookings") && method.equals("POST")) {
            if (!json.hasNonNull("itemId") || !json.get("itemId").canConvertToLong()) {
                throw new GatewayValidationException("Необходимо указать идентификатор вещи");
            }
            LocalDateTime start = date(json, "start", "Необходимо указать дату начала");
            LocalDateTime end = date(json, "end", "Необходимо указать дату окончания");
            LocalDateTime now = LocalDateTime.now();
            if (start.isBefore(now)) {
                throw new GatewayValidationException("Дата начала должна быть в настоящем или будущем");
            }
            if (!end.isAfter(now)) {
                throw new GatewayValidationException("Дата окончания должна быть в будущем");
            }
            if (!end.isAfter(start)) {
                throw new GatewayValidationException("Окончание бронирования должно быть позже начала");
            }
        }
        validatePagination(request, path);
    }

    private String requiredText(JsonNode json, String field, String message, int maxLength) {
        JsonNode value = json.get(field);
        if (value == null || !value.isTextual() || !StringUtils.hasText(value.asText())) {
            throw new GatewayValidationException(message);
        }
        if (value.asText().length() > maxLength) {
            throw new GatewayValidationException(field + " превышает допустимую длину");
        }
        return value.asText();
    }

    private String optionalText(JsonNode json, String field, String message, int maxLength) {
        JsonNode value = json.get(field);
        if (value == null || value.isNull()) {
            return null;
        }
        return requiredText(json, field, message, maxLength);
    }

    private LocalDateTime date(JsonNode json, String field, String message) {
        JsonNode value = json.get(field);
        if (value == null || !value.isTextual() || !StringUtils.hasText(value.asText())) {
            throw new GatewayValidationException(message);
        }
        try {
            return LocalDateTime.parse(value.asText());
        } catch (DateTimeParseException exception) {
            throw new GatewayValidationException("Некорректный формат даты: " + field);
        }
    }

    private void validatePagination(HttpServletRequest request, String path) {
        if (!path.startsWith("/requests")) {
            return;
        }
        String from = request.getParameter("from");
        String size = request.getParameter("size");
        try {
            if (from != null && Integer.parseInt(from) < 0) {
                throw new GatewayValidationException("from должен быть неотрицательным");
            }
            if (size != null && Integer.parseInt(size) <= 0) {
                throw new GatewayValidationException("size должен быть больше нуля");
            }
        } catch (NumberFormatException exception) {
            throw new GatewayValidationException("from и size должны быть целыми числами");
        }
    }
}
