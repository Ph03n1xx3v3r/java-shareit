package ru.practicum.shareit.integrationTest;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingSpecificationDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

@Transactional
@SpringBootTest(
        properties = "jdbc.url=jdbc:postgresql://localhost:5432/test",
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class BookingServiceImplTest {

    private final EntityManager em;
    private final ItemService itemService;
    private final UserService userService;
    private final BookingService bookingService;
    private LocalDateTime start;
    private LocalDateTime end;

    @BeforeEach
    void setUp() {
        start = LocalDateTime.now();
        end = LocalDateTime.now().plusDays(5);
    }


    @Test
    void createBooking() {
        UserDto userDto = makeUserDto("some@email.com", "Jhon");
        Long userId = userService.createUser(userDto).getId();
        ItemDto itemDto = makeItemDto("TV description", "TV", true);
        Long itemId = itemService.createItem(userId, itemDto).getId();

        BookingDto bookingDto = makeBookingDto(itemId, start, end);
        Long bookingId = bookingService.createBooking(bookingDto, userId).getId();
        Booking booking = em.find(Booking.class, bookingId);

        assertThat(booking.getId(), notNullValue());
        assertThat(booking.getItem().getId(), equalTo(itemId));
        assertThat(booking.getStart(), equalTo(start));
        assertThat(booking.getEnd(), equalTo(end));
    }

    @Test
    void getBookingById() {
        UserDto userDto = makeUserDto("some@email.com", "Jhon");
        Long userId = userService.createUser(userDto).getId();
        ItemDto itemDto = makeItemDto("TV description", "TV", true);
        Long itemId = itemService.createItem(userId, itemDto).getId();

        BookingDto bookingDto = makeBookingDto(itemId, start, end);
        Long bookingId = bookingService.createBooking(bookingDto, userId).getId();
        BookingSpecificationDto currentSpecificationDto = bookingService.getBookingById(bookingId, userId);

        assertThat(currentSpecificationDto.getId(), notNullValue());
        assertThat(currentSpecificationDto.getItem().getId(), equalTo(itemId));
        assertThat(currentSpecificationDto.getStart(), equalTo(start));
        assertThat(currentSpecificationDto.getEnd(), equalTo(end));
    }

    private ItemDto makeItemDto(String description, String name, Boolean available) {
        ItemDto dto = new ItemDto();
        dto.setDescription(description);
        dto.setName(name);
        dto.setAvailable(available);
        return dto;
    }

    private UserDto makeUserDto(String email, String name) {
        UserDto dto = new UserDto();
        dto.setEmail(email);
        dto.setName(name);
        return dto;
    }

    private BookingDto makeBookingDto(Long itemId, LocalDateTime start, LocalDateTime end) {
        BookingDto dto = new BookingDto();
        dto.setItemId(itemId);
        dto.setStart(start);
        dto.setEnd(end);
        return dto;
    }

}


