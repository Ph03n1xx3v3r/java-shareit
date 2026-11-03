package ru.practicum.shareit.unitTest;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.request.ItemRequestController;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.RequestWithItemDto;
import ru.practicum.shareit.request.requestService.RequestService;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ItemRequestController.class)
public class ItemRequestControllerTest {
    @MockBean
    @Qualifier("requestServiceImpl")
    RequestService requestService;

    @Autowired
    ObjectMapper mapper;

    @Autowired
    private MockMvc mvc;

    private ItemRequestDto itemRequestDto;
    private RequestWithItemDto requestWithItemDto;
    private LocalDateTime start;
    private LocalDateTime end;
    private LocalDateTime created;

    @BeforeEach
    void setUp() {
        start = LocalDateTime.now();
        end = LocalDateTime.now().plusDays(5);
        created = LocalDateTime.now();
        itemRequestDto = new ItemRequestDto(
                1L,
                "need TV",
                created
        );
        requestWithItemDto = new RequestWithItemDto(
                1L,
                "need TV",
                created,
                null
        );
    }

    @Test
    void create() throws Exception {
        when(requestService.create(itemRequestDto, 1L))
                .thenReturn(itemRequestDto);

        mvc.perform(post("/requests")
                        .content(mapper.writeValueAsString(itemRequestDto))
                        .header("X-Sharer-User-Id", 1L)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(itemRequestDto.getId()), Long.class))
                .andExpect(jsonPath("$.description", is(itemRequestDto.getDescription().toString()), String.class));
    }

    @Test
    void getAllRequestWithoutUsersRequest() throws Exception {
        when(requestService.getAllRequestWithoutUsersRequest(1L))
                .thenReturn(List.of(itemRequestDto));

        mvc.perform(get("/requests/all")
                        .header("X-Sharer-User-Id", 1L)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", is(itemRequestDto.getId()), Long.class))
                .andExpect(jsonPath("$[0].description", is(itemRequestDto.getDescription().toString()), String.class));
    }

    @Test
    void getItemRequestsByUserId() throws Exception {
        when(requestService.getItemRequestsByUserId(1L))
                .thenReturn(List.of(requestWithItemDto));

        mvc.perform(get("/requests")
                        .header("X-Sharer-User-Id", 1L)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", is(requestWithItemDto.getId()), Long.class))
                .andExpect(jsonPath("$[0].description", is(requestWithItemDto.getDescription().toString()), String.class));
    }

    @Test
    void getItemRequestsById() throws Exception {
        when(requestService.getItemRequestsById(1L, 1L))
                .thenReturn(requestWithItemDto);

        mvc.perform(get("/requests/" + 1L)
                        .header("X-Sharer-User-Id", 1L)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(requestWithItemDto.getId()), Long.class))
                .andExpect(jsonPath("$.description", is(requestWithItemDto.getDescription().toString()), String.class));
    }

}
