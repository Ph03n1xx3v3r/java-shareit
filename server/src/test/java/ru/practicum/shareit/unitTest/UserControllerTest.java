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
import ru.practicum.shareit.exception.ConditionsNotMetException;
import ru.practicum.shareit.exception.DuplicatedDataException;
import ru.practicum.shareit.exception.ObjectNotFoundException;
import ru.practicum.shareit.user.UserController;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;

import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = UserController.class)
public class UserControllerTest {
    @MockBean
    @Qualifier("userServiceImpl")
    private UserService userService;

    @Autowired
    ObjectMapper mapper;

    @Autowired
    private MockMvc mvc;

    private UserDto userDto;

    @BeforeEach
    void setUp() {
        userDto = new UserDto(
                1L,
                "John",
                "john.doe@mail.com"
        );
    }

    @Test
    void createUser() throws Exception {
        when(userService.createUser(any()))
                .thenReturn(userDto);

        mvc.perform(post("/users")
                        .content(mapper.writeValueAsString(userDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(userDto.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(userDto.getName())))
                .andExpect(jsonPath("$.email", is(userDto.getEmail())));
    }

    @Test
    void updateUser() throws Exception {
        when(userService.updateUser(any(), anyLong()))
                .thenReturn(userDto);

        mvc.perform(patch("/users/" + 1L)
                        .content(mapper.writeValueAsString(userDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(userDto.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(userDto.getName())))
                .andExpect(jsonPath("$.email", is(userDto.getEmail())));
    }

    @Test
    void deleteUser() throws Exception {
        Map<String, String> resp = Map.of(
                "status", "success",
                "operation", "Delete user"
        );
        when(userService.deleteUser(anyLong()))
                .thenReturn(resp);

        mvc.perform(delete("/users/" + 1L)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is(resp.get("status")), String.class))
                .andExpect(jsonPath("$.operation", is(resp.get("operation")), String.class));
    }

    @Test
    void getUser() throws Exception {

        when(userService.getUserById(anyLong()))
                .thenReturn(userDto);

        mvc.perform(get("/users/" + 1L)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(userDto.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(userDto.getName())))
                .andExpect(jsonPath("$.email", is(userDto.getEmail())));
    }

    @Test
    void createUserWithIllegalArgumentException() throws Exception {

        when(userService.createUser(any()))
                .thenThrow(IllegalArgumentException.class);

        mvc.perform(post("/users")
                        .content(mapper.writeValueAsString(userDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(500));
    }

    @Test
    void createUserWithDuplicatedDataException() throws Exception {

        when(userService.createUser(any()))
                .thenThrow(new DuplicatedDataException("Этот имейл уже используется"));

        mvc.perform(post("/users")
                        .content(mapper.writeValueAsString(userDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(409))
                .andExpect(jsonPath("$.description").value("Ошибка с входным параметром."))
                .andExpect(jsonPath("$.error").value("Этот имейл уже используется"));
    }

    @Test
    void updateUserWithConditionsNotMetException() throws Exception {

        when(userService.createUser(any()))
                .thenThrow(new ConditionsNotMetException("Не корректные входные данные"));

        mvc.perform(post("/users")
                        .content(mapper.writeValueAsString(userDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(400))
                .andExpect(jsonPath("$.description").value("Ошибка с входным параметром."))
                .andExpect(jsonPath("$.error").value("Не корректные входные данные"));
    }

    @Test
    void updateUserWithObjectNotFoundException() throws Exception {

        when(userService.createUser(any()))
                .thenThrow(new ObjectNotFoundException("Пользователя по указаному id не существует - "));

        mvc.perform(post("/users")
                        .content(mapper.writeValueAsString(userDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(404))
                .andExpect(jsonPath("$.description").value("Объект не найден."))
                .andExpect(jsonPath("$.error").value("Пользователя по указаному id не существует - "));
    }

    @Test
    void updateUserWithConditionsNotMetExceptionIncorrectData() throws Exception {

        when(userService.createUser(any()))
                .thenThrow(new ConditionsNotMetException("Не корректное тело запроса"));

        mvc.perform(post("/users")
                        .content(mapper.writeValueAsString(userDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(400))
                .andExpect(jsonPath("$.description").value("Ошибка с входным параметром."))
                .andExpect(jsonPath("$.error").value("Не корректное тело запроса"));
    }

    @Test
    void deleteUserWithObjectNotFoundException() throws Exception {

        when(userService.deleteUser(any()))
                .thenThrow(new ObjectNotFoundException("Пользователя по указаному id не существует - "));

        mvc.perform(delete("/users/" + 1L)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(404))
                .andExpect(jsonPath("$.description").value("Объект не найден."))
                .andExpect(jsonPath("$.error").value("Пользователя по указаному id не существует - "));
    }

    @Test
    void getUserWithObjectNotFoundException() throws Exception {

        when(userService.getUserById(anyLong()))
                .thenThrow(new ObjectNotFoundException("Пользователя по указаному id не существует - "));

        mvc.perform(get("/users/" + 1L)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(404))
                .andExpect(jsonPath("$.description").value("Объект не найден."))
                .andExpect(jsonPath("$.error").value("Пользователя по указаному id не существует - "));
    }

}
