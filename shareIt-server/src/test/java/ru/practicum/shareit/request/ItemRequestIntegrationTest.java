package ru.practicum.shareit.request;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ItemRequestIntegrationTest {
    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void requestCanBeCreatedAndAnsweredByItem() throws Exception {
        long requestorId = createUser("requestor@example.com");
        long ownerId = createUser("owner@example.com");

        String requestResponse = mvc.perform(post("/requests")
                        .header("X-Sharer-User-Id", requestorId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"description\":\"Нужна дрель\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description").value("Нужна дрель"))
                .andExpect(jsonPath("$.created").exists())
                .andExpect(jsonPath("$.items", hasSize(0)))
                .andReturn().getResponse().getContentAsString();

        long requestId = objectMapper.readTree(requestResponse).get("id").asLong();

        String itemResponse = mvc.perform(post("/items")
                        .header("X-Sharer-User-Id", ownerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Дрель\",\"description\":\"Электрическая дрель\","
                                + "\"available\":true,\"requestId\":" + requestId + "}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.requestId").value(requestId))
                .andReturn().getResponse().getContentAsString();

        long itemId = objectMapper.readTree(itemResponse).get("id").asLong();

        mvc.perform(get("/requests/{requestId}", requestId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(1)))
                .andExpect(jsonPath("$.items[0].id").value(itemId))
                .andExpect(jsonPath("$.items[0].name").value("Дрель"))
                .andExpect(jsonPath("$.items[0].ownerId").value(ownerId));

        mvc.perform(get("/requests")
                        .header("X-Sharer-User-Id", requestorId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(requestId));

        mvc.perform(get("/requests/all")
                        .header("X-Sharer-User-Id", ownerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(requestId));
    }

    @Test
    void requestDescriptionMustNotBeBlank() throws Exception {
        long userId = createUser("validation@example.com");
        mvc.perform(post("/requests")
                        .header("X-Sharer-User-Id", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"description\":\"   \"}"))
                .andExpect(status().isBadRequest());
    }

    private long createUser(String email) throws Exception {
        String response = mvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Test User\",\"email\":\"" + email + "\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        JsonNode user = objectMapper.readTree(response);
        return user.get("id").asLong();
    }
}
