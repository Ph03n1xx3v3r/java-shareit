package ru.practicum.shareit.gateway;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.RestTemplate;

import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ValidationProxyController.class)
@ContextConfiguration(classes = ShareItGatewayApp.class)
class ValidationProxyControllerTest {
    @Autowired
    private MockMvc mvc;

    @MockBean
    private RestTemplate restTemplate;

    @Test
    void invalidRequestIsRejectedBeforeServerCall() throws Exception {
        mvc.perform(post("/requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"description\":\"   \"}"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(restTemplate);
    }

    @Test
    void malformedJsonIsRejectedBeforeServerCall() throws Exception {
        mvc.perform(post("/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{invalid json"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(restTemplate);
    }
}
