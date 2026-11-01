package ru.practicum.shareit.integrationTest;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import java.util.Map;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Transactional
@SpringBootTest(
        properties = "jdbc.url=jdbc:postgresql://localhost:5432/test",
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class UserServiceImplTest {

    private final EntityManager em;
    private final UserService service;

    @Test
    void createUser() {
        UserDto userDto = makeUserDto("some@email.com", "Jhon");

        service.createUser(userDto);

        TypedQuery<User> query = em.createQuery("Select u from User u where u.email = :email", User.class);
        User user = query.setParameter("email", userDto.getEmail())
                .getSingleResult();

        assertThat(user.getId(), notNullValue());
        assertThat(user.getName(), equalTo(userDto.getName()));
        assertThat(user.getEmail(), equalTo(userDto.getEmail()));
    }

    @Test
    void getUserById() {
        UserDto userDto = makeUserDto("some@email.com", "Jhon");
        Long userId = service.createUser(userDto).getId();
        UserDto currentUserDto = service.getUserById(userId);
        assertThat(currentUserDto.getId(), equalTo(userId));
        assertThat(currentUserDto.getName(), equalTo(userDto.getName()));
        assertThat(currentUserDto.getEmail(), equalTo(userDto.getEmail()));
    }

    @Test
    void updateUser() {
        UserDto userDto = makeUserDto("some@email.com", "Jhon");
        Long userId = service.createUser(userDto).getId();
        Map<String, String> updateMap = Map.of(
                "email", "new@email.com"
        );
        UserDto currentUserDto = service.updateUser(updateMap, userId);
        assertThat(currentUserDto.getId(), equalTo(userId));
        assertThat(currentUserDto.getEmail(), equalTo("new@email.com"));
        assertThat(currentUserDto.getName(), equalTo(userDto.getName()));
    }

    @Test
    void deleteUser() {
        UserDto userDto = makeUserDto("some@email.com", "Jhon");
        Long userId = service.createUser(userDto).getId();
        service.deleteUser(userId);
        TypedQuery<User> query = em.createQuery("Select u from User u where u.id = :userId", User.class);
        User user = em.find(User.class, userId);
        assertThat(user, nullValue());
    }

    @Test
    void checkObjectOwnerAndGetUserEntityById() {
        UserDto userDto = makeUserDto("some@email.com", "Jhon");
        service.createUser(userDto);
        TypedQuery<Long> query = em.createQuery("Select max(u.id) from User u ", Long.class);
        Long maxUserId = query.getSingleResult();
        assertThrows(ForbiddenException.class,
                () -> service.checkObjectOwnerAndGetUserEntityById(maxUserId + 1)
        );
    }

    private UserDto makeUserDto(String email, String name) {
        UserDto dto = new UserDto();
        dto.setEmail(email);
        dto.setName(name);
        return dto;
    }
}