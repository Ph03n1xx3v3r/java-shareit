package ru.practicum.shareit.item;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.*;
import ru.practicum.shareit.item.service.ItemService;

import java.util.Collection;
import java.util.Map;

@RestController
@RequestMapping("/items")
public class ItemController {
    ItemService itemService;

    public ItemController(@Qualifier("itemServiceImpl") ItemService itemService) {
        this.itemService = itemService;
    }

    @PostMapping
    public ItemDto createItem(@RequestHeader("X-Sharer-User-Id") Long userId,
                              @RequestBody ItemDto itemDto) {
        return itemService.createItem(userId, itemDto);
    }

    @PatchMapping("/{itemId}")
    public ItemDto updateItem(@RequestHeader("X-Sharer-User-Id") Long userId,
                              @RequestBody Map<String, String> updatesItem,
                              @PathVariable Long itemId) {
        return itemService.updateItem(userId, updatesItem, itemId);
    }

    @GetMapping("/{itemId}")
    public ItemWithBookingDto getItemById(@PathVariable Long itemId) {
        return itemService.getItemById(itemId);
    }

    @GetMapping()
    public Collection<ItemListDto> getAllItemsFromUser(@RequestHeader("X-Sharer-User-Id") Long userId) {
        return itemService.getAllItemsFromUser(userId);
    }

    @GetMapping("/search")
    public Collection<ItemSpecificationDto> searchItemsForUser(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                               @RequestParam String text) {
        return itemService.searchItemsForUser(userId, text);
    }

    @PostMapping("/{itemId}/comment")
    public CommentDto addCommentForItem(@RequestHeader("X-Sharer-User-Id") Long userId,
                                        @RequestBody CommentDto commentDto,
                                        @PathVariable Long itemId) {
        return itemService.addCommentForItem(userId, itemId, commentDto);
    }
}
