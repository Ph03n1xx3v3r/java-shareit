package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.*;

import java.util.Collection;
import java.util.Map;

public interface ItemService {

    ItemDto createItem(Long userId, ItemDto itemDto);

    ItemDto updateItem(Long userId, Map<String, String> updatesItem, Long itemId);

    ItemWithBookingDto getItemById(Long itemId);

    Collection<ItemListDto> getAllItemsFromUser(Long userId);

    Collection<ItemSpecificationDto> searchItemsForUser(Long userId, String text);

    CommentDto addCommentForItem(Long userId, Long itemId, CommentDto commentDto);
}
