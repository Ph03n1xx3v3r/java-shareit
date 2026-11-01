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
import ru.practicum.shareit.booking.BookingController;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingSpecificationDto;
import ru.practicum.shareit.booking.model.State;
import ru.practicum.shareit.booking.model.StatusBooking;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.exception.ForbiddenException;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = BookingController.class)
public class BookingControllerTest {
    @MockBean
    @Qualifier("bookingServiceImpl")
    BookingService bookingService;

    @Autowired
    ObjectMapper mapper;

    @Autowired
    private MockMvc mvc;

    private BookingDto bookingDto;
    private BookingSpecificationDto bookingSpecificationDto;
    private LocalDateTime start;
    private LocalDateTime end;
    private LocalDateTime created;

    @BeforeEach
    void setUp() {
        start = LocalDateTime.now();
        end = LocalDateTime.now().plusDays(5);
        created = LocalDateTime.now();
        bookingDto = new BookingDto(
                start,
                end,
                1L
        );
        bookingSpecificationDto = new BookingSpecificationDto(
                1L,
                start,
                end,
                null,
                null,
                StatusBooking.WAITING
        );
    }

    @Test
    void createBooking() throws Exception {
        when(bookingService.createBooking(bookingDto, 1L))
                .thenReturn(bookingSpecificationDto);

        mvc.perform(post("/bookings")
                        .content(mapper.writeValueAsString(bookingDto))
                        .header("X-Sharer-User-Id", 1L)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(bookingSpecificationDto.getId()), Long.class))
                .andExpect(jsonPath("$.status", is(bookingSpecificationDto.getStatus().toString()), String.class));
    }

    @Test
    void approveBooking() throws Exception {
        when(bookingService.approveBooking(1L, 1L, true))
                .thenReturn(bookingSpecificationDto);

        mvc.perform(patch("/bookings/" + 1L)
                        .content(mapper.writeValueAsString(bookingDto))
                        .param("approved", "true")
                        .header("X-Sharer-User-Id", 1L)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(bookingSpecificationDto.getId()), Long.class))
                .andExpect(jsonPath("$.status", is(bookingSpecificationDto.getStatus().toString()), String.class));
    }

    @Test
    void getBookingById() throws Exception {
        when(bookingService.getBookingById(1L, 1L))
                .thenReturn(bookingSpecificationDto);

        mvc.perform(get("/bookings/" + 1L)
                        .header("X-Sharer-User-Id", 1L)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(bookingSpecificationDto.getId()), Long.class))
                .andExpect(jsonPath("$.status", is(bookingSpecificationDto.getStatus().toString()), String.class));
    }

    @Test
    void getAllBookingByUser() throws Exception {
        when(bookingService.getAllBookingByUser(1L, State.ALL))
                .thenReturn(List.of(bookingSpecificationDto));

        mvc.perform(get("/bookings")
                        .param("state", "ALL")
                        .header("X-Sharer-User-Id", 1L)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", is(bookingSpecificationDto.getId()), Long.class))
                .andExpect(jsonPath("$[0].status", is(bookingSpecificationDto.getStatus().toString()), String.class));
    }

    @Test
    void getAllBookingByItemOwner() throws Exception {
        when(bookingService.getAllBookingByItemOwner(1L, State.ALL))
                .thenReturn(List.of(bookingSpecificationDto));

        mvc.perform(get("/bookings/owner")
                        .param("state", "ALL")
                        .header("X-Sharer-User-Id", 1L)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", is(bookingSpecificationDto.getId()), Long.class))
                .andExpect(jsonPath("$[0].status", is(bookingSpecificationDto.getStatus().toString()), String.class));
    }

    @Test
    void getAllBookingByItemOwnerWithoutState() throws Exception {
        when(bookingService.getAllBookingByItemOwner(1L, State.ALL))
                .thenReturn(List.of(bookingSpecificationDto));

        mvc.perform(get("/bookings/owner")
                        .header("X-Sharer-User-Id", 1L)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", is(bookingSpecificationDto.getId()), Long.class))
                .andExpect(jsonPath("$[0].status", is(bookingSpecificationDto.getStatus().toString()), String.class));
    }

    @Test
    void getAllBookingByUserWithoutState() throws Exception {
        when(bookingService.getAllBookingByUser(1L, State.ALL))
                .thenReturn(List.of(bookingSpecificationDto));

        mvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", 1L)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", is(bookingSpecificationDto.getId()), Long.class))
                .andExpect(jsonPath("$[0].status", is(bookingSpecificationDto.getStatus().toString()), String.class));
    }

    @Test
    void getBookingByIdWithForbiddenException() throws Exception {
        when(bookingService.getBookingById(1L, 1L))
                .thenThrow(new ForbiddenException("Доступ к данным запрещен для пользователя с id - "));

        mvc.perform(get("/bookings/" + 1L)
                        .header("X-Sharer-User-Id", 1L)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(403))
                .andExpect(jsonPath("$.description").value("Что-то пошло не так. Обратитесь к системному администратору"))
                .andExpect(jsonPath("$.error").value("Доступ к данным запрещен для пользователя с id - "));
    }
}
