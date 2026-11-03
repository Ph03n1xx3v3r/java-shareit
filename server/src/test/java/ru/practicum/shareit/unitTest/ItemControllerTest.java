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
import ru.practicum.shareit.exception.ObjectNotFoundException;
import ru.practicum.shareit.item.ItemController;
import ru.practicum.shareit.item.dto.*;
import ru.practicum.shareit.item.service.ItemService;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ItemController.class)
public class ItemControllerTest {
    @MockBean
    @Qualifier("itemServiceImpl")
    ItemService itemService;

    @Autowired
    ObjectMapper mapper;

    @Autowired
    private MockMvc mvc;

    private ItemDto itemDto;
    private CommentDto commentDto;
    private ItemListDto itemListDto;
    private ItemWithBookingDto itemWithBookingDto;
    private ItemSpecificationDto itemSpecificationDto;
    private LocalDateTime lastBooking;
    private LocalDateTime nextBooking;
    private LocalDateTime created;

    @BeforeEach
    void setUp() {
        lastBooking = LocalDateTime.now();
        nextBooking = LocalDateTime.now();
        created = LocalDateTime.now();
        itemDto = new ItemDto(
                1L,
                "TV",
                "Big cool TV",
                true,
                null
        );
        itemWithBookingDto = new ItemWithBookingDto(
                1L,
                "TV",
                "Big cool TV",
                lastBooking,
                nextBooking,
                true,
                null
        );
        itemListDto = new ItemListDto(
                1L,
                "TV",
                "Big cool TV",
                lastBooking,
                nextBooking,
                null
        );
        itemSpecificationDto = new ItemSpecificationDto(
                "TV",
                "Big cool TV"
        );
        commentDto = new CommentDto(
                1L,
                "exampleComment",
                "Jhon",
                created
        );
    }

    @Test
    void createItem() throws Exception {
        when(itemService.createItem(anyLong(), any()))
                .thenReturn(itemDto);

        mvc.perform(post("/items")
                        .content(mapper.writeValueAsString(itemDto))
                        .header("X-Sharer-User-Id", 1L)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(itemDto.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(itemDto.getName())))
                .andExpect(jsonPath("$.description", is(itemDto.getDescription())))
                .andExpect(jsonPath("$.available", is(itemDto.getAvailable())))
                .andExpect(jsonPath("$.requestId", is(itemDto.getRequestId())));
    }

    @Test
    void updateItem() throws Exception {
        when(itemService.updateItem(anyLong(), any(), anyLong()))
                .thenReturn(itemDto);

        mvc.perform(patch("/items/1")
                        .content(mapper.writeValueAsString(itemDto))
                        .header("X-Sharer-User-Id", 1L)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(itemDto.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(itemDto.getName())))
                .andExpect(jsonPath("$.description", is(itemDto.getDescription())))
                .andExpect(jsonPath("$.available", is(itemDto.getAvailable())))
                .andExpect(jsonPath("$.requestId", is(itemDto.getRequestId())));
    }

    @Test
    void getItemById() throws Exception {
        when(itemService.getItemById(anyLong()))
                .thenReturn(itemWithBookingDto);

        mvc.perform(get("/items/1")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(itemWithBookingDto.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(itemWithBookingDto.getName())))
                .andExpect(jsonPath("$.description", is(itemWithBookingDto.getDescription())))
                .andExpect(jsonPath("$.available", is(itemWithBookingDto.getAvailable())));
    }

    @Test
    void getAllItemsFromUser() throws Exception {
        when(itemService.getAllItemsFromUser(1L))
                .thenReturn(List.of(itemListDto));

        mvc.perform(get("/items")
                        .header("X-Sharer-User-Id", 1L)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", is(itemListDto.getId()), Long.class))
                .andExpect(jsonPath("$[0].name", is(itemListDto.getName())))
                .andExpect(jsonPath("$[0].description", is(itemListDto.getDescription())));
    }

    @Test
    void searchItemsForUser() throws Exception {
        when(itemService.searchItemsForUser(1L, "exampleText"))
                .thenReturn(List.of(itemSpecificationDto));

        mvc.perform(get("/items/search")
                        .header("X-Sharer-User-Id", 1L)
                        .param("text", "exampleText")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name", is(itemSpecificationDto.getName())))
                .andExpect(jsonPath("$[0].description", is(itemSpecificationDto.getDescription())));
    }

    @Test
    void addCommentForItem() throws Exception {
        when(itemService.addCommentForItem(anyLong(), anyLong(), any()))
                .thenReturn(commentDto);

        mvc.perform(post("/items/" + 1L + "/comment")
                        .content(mapper.writeValueAsString(commentDto))
                        .header("X-Sharer-User-Id", 1L)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(commentDto.getId()), Long.class))
                .andExpect(jsonPath("$.text", is(commentDto.getText())))
                .andExpect(jsonPath("$.authorName", is(commentDto.getAuthorName())));
    }

    @Test
    void createItemWithIncorrectRequestBody() throws Exception {
        mvc.perform(post("/items")
                        .header("X-Sharer-User-Id", 1L)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void createItemWithIllegalArgumentException() throws Exception {
        when(itemService.createItem(anyLong(), any()))
                .thenThrow(IllegalArgumentException.class);

        mvc.perform(post("/items")
                        .content(mapper.writeValueAsString(itemDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(400));
    }

    @Test
    void createItemWithObjectNotFoundException() throws Exception {
        when(itemService.createItem(anyLong(), any()))
                .thenThrow(new ObjectNotFoundException("Пользователя по указаному id не существует - "));

        mvc.perform(post("/items")
                        .content(mapper.writeValueAsString(itemDto))
                        .header("X-Sharer-User-Id", 1L)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(404))
                .andExpect(jsonPath("$.description").value("Объект не найден."))
                .andExpect(jsonPath("$.error").value("Пользователя по указаному id не существует - "));
    }

    @Test
    void createItemWithoutUserId() throws Exception {
        when(itemService.createItem(anyLong(), any()))
                .thenThrow(new ObjectNotFoundException("Пользователя по указаному id не существует - "));

        mvc.perform(post("/items")
                        .content(mapper.writeValueAsString(itemDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(400));
    }
}
