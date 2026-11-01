package ru.practicum.shareit.integrationTest;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;

import java.util.Map;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

@Transactional
@SpringBootTest(
        properties = "jdbc.url=jdbc:postgresql://localhost:5432/test",
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ItemServiceImpl {
    private final EntityManager em;
    private final ItemService itemService;
    private final UserService userService;

    @Test
    void createItem() {
        UserDto userDto = makeUserDto("some@email.com", "Jhon");
        Long userId = userService.createUser(userDto).getId();
        ItemDto itemDto = makeItemDto("TV description", "TV", true);
        ItemDto currentItemDto = itemService.createItem(userId, itemDto);
        Item item = em.find(Item.class, currentItemDto.getId());
        assertThat(item.getId(), notNullValue());
        assertThat(item.getName(), equalTo(itemDto.getName()));
        assertThat(item.getDescription(), equalTo(itemDto.getDescription()));
    }

    @Test
    void updateItem() {
        UserDto userDto = makeUserDto("some@email.com", "Jhon");
        Long userId = userService.createUser(userDto).getId();
        ItemDto itemDto = makeItemDto("TV description", "TV", true);
        ItemDto currentItemDto = itemService.createItem(userId, itemDto);

        Map<String, String> updateMap = Map.of(
                "description", "New description"
        );

        itemService.updateItem(userId, updateMap, currentItemDto.getId());
        Item item = em.find(Item.class, currentItemDto.getId());
        assertThat(item.getId(), notNullValue());
        assertThat(item.getName(), equalTo(itemDto.getName()));
        assertThat(item.getDescription(), equalTo("New description"));
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

}
