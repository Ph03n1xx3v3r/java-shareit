package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserService;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {
    private final ItemStorage itemStorage;
    private final UserService userService;

    @Override
    public ItemDto create(long userId, ItemDto itemDto) {
        validateForCreate(itemDto);
        User owner = userService.getUser(userId);

        Item item = ItemMapper.toItem(itemDto);
        item.setOwner(owner);

        return ItemMapper.toItemDto(itemStorage.create(item));
    }

    @Override
    public ItemDto update(long userId, long itemId, ItemDto itemDto) {
        userService.getUser(userId);
        Item item = getItem(itemId);

        if (!item.getOwner().getId().equals(userId)) {
            throw new NotFoundException("Пользователь с id=" + userId + " не является владельцем вещи");
        }

        if (itemDto.getName() != null) {
            validateText(itemDto.getName(), "Название вещи не может быть пустым");
            item.setName(itemDto.getName());
        }

        if (itemDto.getDescription() != null) {
            validateText(itemDto.getDescription(), "Описание вещи не может быть пустым");
            item.setDescription(itemDto.getDescription());
        }

        if (itemDto.getAvailable() != null) {
            item.setAvailable(itemDto.getAvailable());
        }

        return ItemMapper.toItemDto(itemStorage.update(item));
    }

    @Override
    public ItemDto getById(long itemId) {
        return ItemMapper.toItemDto(getItem(itemId));
    }

    @Override
    public List<ItemDto> getAllByOwner(long userId) {
        userService.getUser(userId);
        return itemStorage.findByOwnerId(userId).stream()
                .map(ItemMapper::toItemDto)
                .toList();
    }

    @Override
    public List<ItemDto> search(String text) {
        return itemStorage.search(text).stream()
                .map(ItemMapper::toItemDto)
                .toList();
    }

    private Item getItem(long itemId) {
        return itemStorage.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Вещь с id=" + itemId + " не найдена"));
    }

    private void validateForCreate(ItemDto itemDto) {
        validateText(itemDto.getName(), "Название вещи не может быть пустым");
        validateText(itemDto.getDescription(), "Описание вещи не может быть пустым");
        if (itemDto.getAvailable() == null) {
            throw new ValidationException("Необходимо указать доступность вещи");
        }
    }

    private void validateText(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new ValidationException(message);
        }
    }
}
