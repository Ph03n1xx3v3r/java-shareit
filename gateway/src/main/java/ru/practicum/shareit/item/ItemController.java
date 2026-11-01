package ru.practicum.shareit.item;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;

import java.util.Map;

@RestController
@RequestMapping("/items")
@Validated
public class ItemController {
    ItemClient itemClient;

    public ItemController(@Qualifier("itemClient") ItemClient itemClient) {
        this.itemClient = itemClient;
    }

    @PostMapping
    public ResponseEntity<Object> createItem(@RequestHeader("X-Sharer-User-Id") Long userId,
                                             @Valid @RequestBody ItemDto itemDto) {
        return itemClient.createItem(userId, itemDto);
    }

    @PatchMapping("/{itemId}")
    public ResponseEntity<Object> updateItem(@RequestHeader("X-Sharer-User-Id") Long userId,
                                             @RequestBody Map<String, String> updatesItem,
                                             @PathVariable Long itemId) {
        return itemClient.updateItem(userId, updatesItem, itemId);
    }

    @GetMapping("/{itemId}")
    public ResponseEntity<Object> getItemById(@PathVariable Long itemId) {
        return itemClient.getItemById(itemId);
    }

    @GetMapping()
    public ResponseEntity<Object> getAllItemsFromUser(@RequestHeader("X-Sharer-User-Id") Long userId) {
        return itemClient.getAllItemsFromUser(userId);
    }

    @GetMapping("/search")
    public ResponseEntity<Object> searchItemsForUser(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                     @RequestParam String text) {
        return itemClient.searchItemsForUser(userId, text);
    }

    @PostMapping("/{itemId}/comment")
    public ResponseEntity<Object> addCommentForItem(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                    @Valid @RequestBody CommentDto commentDto,
                                                    @PathVariable Long itemId) {
        return itemClient.addCommentForItem(userId, commentDto, itemId);
    }
}
