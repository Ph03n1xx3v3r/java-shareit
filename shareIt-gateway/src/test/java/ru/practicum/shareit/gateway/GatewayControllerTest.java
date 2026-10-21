package ru.practicum.shareit.gateway;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class GatewayControllerTest {
    private BaseClient client;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        client = mock(BaseClient.class);
        GatewayController controller = new GatewayController(client, new GatewayRequestValidator(), new ObjectMapper());
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
        when(client.forward(any(HttpMethod.class), anyString(), any(), any(HttpHeaders.class), any(byte[].class)))
                .thenReturn(ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON)
                        .body("{}".getBytes(StandardCharsets.UTF_8)));
    }

    @Test
    void forwardsCreateRequestAfterGatewayValidation() throws Exception {
        mockMvc.perform(post("/requests")
                        .header("X-Sharer-User-Id", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"description\":\"Need a drill\"}"))
                .andExpect(status().isOk())
                .andExpect(content().json("{}"));
        verify(client).forward(eq(HttpMethod.POST), eq("/requests"), any(), any(HttpHeaders.class), any(byte[].class));
    }

    @Test
    void forwardsOwnAndAllRequestsWithUserHeader() throws Exception {
        mockMvc.perform(get("/requests").header("X-Sharer-User-Id", "1"))
                .andExpect(status().isOk());
        mockMvc.perform(get("/requests/all").header("X-Sharer-User-Id", "1"))
                .andExpect(status().isOk());
        verify(client).forward(eq(HttpMethod.GET), eq("/requests"), any(), any(HttpHeaders.class), any(byte[].class));
        verify(client).forward(eq(HttpMethod.GET), eq("/requests/all"), any(), any(HttpHeaders.class), any(byte[].class));
    }

    @Test
    void allowsAnyUserToReadRequestById() throws Exception {
        mockMvc.perform(get("/requests/12")).andExpect(status().isOk());
        verify(client).forward(eq(HttpMethod.GET), eq("/requests/12"), any(), any(HttpHeaders.class), any(byte[].class));
    }

    @Test
    void forwardsItemWithOptionalRequestId() throws Exception {
        mockMvc.perform(post("/items")
                        .header("X-Sharer-User-Id", "2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Drill\",\"description\":\"Cordless drill\",\"available\":true,\"requestId\":12}"))
                .andExpect(status().isOk());
        verify(client).forward(eq(HttpMethod.POST), eq("/items"), any(), any(HttpHeaders.class), any(byte[].class));
    }

    @Test
    void rejectsInvalidRequestBeforeForwarding() throws Exception {
        reset(client);
        mockMvc.perform(post("/requests")
                        .header("X-Sharer-User-Id", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"description\":\" \"}"))
                .andExpect(status().isBadRequest());
        org.mockito.Mockito.verifyNoInteractions(client);
    }
}
