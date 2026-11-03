package ru.practicum.shareit.integrationTest;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.model.Request;
import ru.practicum.shareit.request.requestService.RequestService;
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
public class RequestServiceImplTest {
    private final EntityManager em;
    private final UserService userService;
    private final RequestService requestService;
    private LocalDateTime created;

    @BeforeEach
    void setUp() {
        created = LocalDateTime.now();
    }


    @Test
    void createItemRequest() {
        UserDto userDto = makeUserDto("some@email.com", "Jhon");
        Long userId = userService.createUser(userDto).getId();

        ItemRequestDto itemRequestDto = makeItemRequestDto("request description", created);
        Long itemRequestId = requestService.create(itemRequestDto, userId).getId();
        Request itemRequest = em.find(Request.class, itemRequestId);

        assertThat(itemRequest.getId(), notNullValue());
        assertThat(itemRequest.getDescription(), equalTo(itemRequestDto.getDescription()));
    }

    private UserDto makeUserDto(String email, String name) {
        UserDto dto = new UserDto();
        dto.setEmail(email);
        dto.setName(name);
        return dto;
    }

    private ItemRequestDto makeItemRequestDto(String description, LocalDateTime created) {
        ItemRequestDto dto = new ItemRequestDto();
        dto.setDescription(description);
        dto.setCreated(created);
        return dto;
    }

}
