package ru.practicum.shareit.gateway;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.util.UriUtils;

import java.net.URI;
import java.util.Map;

@Component
public class BaseClient {
    private final RestTemplate restTemplate = new RestTemplate();
    private final String serverUrl;

    public BaseClient(@Value("${shareit-server.url:http://localhost:9090}") String serverUrl) {
        this.serverUrl = serverUrl;
    }

    public ResponseEntity<byte[]> forward(HttpMethod method, String path, MultiValueMap<String, String> query,
                                          HttpHeaders incomingHeaders, byte[] body) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(serverUrl).path(path);
        if (query != null) {
            for (Map.Entry<String, java.util.List<String>> entry : query.entrySet()) {
                for (String value : entry.getValue()) {
                    builder.queryParam(entry.getKey(), UriUtils.encodeQueryParam(value, java.nio.charset.StandardCharsets.UTF_8));
                }
            }
        }
        URI uri = builder.build(true).toUri();
        HttpHeaders headers = new HttpHeaders();
        if (incomingHeaders != null) {
            incomingHeaders.forEach((name, values) -> {
                if (!name.equalsIgnoreCase("host") && !name.equalsIgnoreCase("content-length")
                        && !name.equalsIgnoreCase("connection")) {
                    headers.put(name, values);
                }
            });
        }
        try {
            return restTemplate.exchange(uri, method, new HttpEntity<>(body, headers), byte[].class);
        } catch (HttpStatusCodeException exception) {
            return ResponseEntity.status(exception.getStatusCode())
                    .headers(exception.getResponseHeaders() == null ? new HttpHeaders() : exception.getResponseHeaders())
                    .body(exception.getResponseBodyAsByteArray());
        }
    }
}
