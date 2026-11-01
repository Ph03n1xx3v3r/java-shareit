package ru.practicum.shareit.gateway;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class GatewayController {
    private final BaseClient client;
    private final GatewayRequestValidator validator;
    private final ObjectMapper objectMapper;

    @RequestMapping({"/", "/**"})
    public ResponseEntity<byte[]> proxy(HttpServletRequest request) throws IOException {
        String path = request.getRequestURI();
        HttpMethod method = HttpMethod.valueOf(request.getMethod());
        LinkedMultiValueMap<String, String> query = new LinkedMultiValueMap<>();
        request.getParameterMap().forEach((key, values) -> {
            for (String value : values) query.add(key, value);
        });
        HttpHeaders headers = new HttpHeaders();
        Collections.list(request.getHeaderNames()).forEach(name ->
                Collections.list(request.getHeaders(name)).forEach(value -> headers.add(name, value)));
        byte[] body = StreamUtils.copyToByteArray(request.getInputStream());

        String userId = request.getHeader("X-Sharer-User-Id");
        var validationError = validator.validate(method.name(), path, userId, body);
        if (validationError.isPresent()) {
            byte[] errorBody = objectMapper.writeValueAsBytes(Map.of("error", validationError.get()));
            return ResponseEntity.badRequest().header(HttpHeaders.CONTENT_TYPE, "application/json")
                    .body(errorBody);
        }
        return client.forward(method, path, query, headers, body);
    }
}
