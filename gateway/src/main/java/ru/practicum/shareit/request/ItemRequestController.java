package ru.practicum.shareit.request;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.ItemRequestDto;

@RestController
@RequestMapping(path = "/requests")
@Validated
public class ItemRequestController {

    ItemRequestClient itemRequestClient;

    public ItemRequestController(@Qualifier("itemRequestClient") ItemRequestClient itemRequestClient) {
        this.itemRequestClient = itemRequestClient;
    }

    @PostMapping
    public ResponseEntity<Object> create(@Valid @RequestBody ItemRequestDto itemRequestDto,
                                         @RequestHeader("X-Sharer-User-Id") Long userId) {

        return itemRequestClient.create(itemRequestDto, userId);
    }

    @GetMapping("/all")
    public ResponseEntity<Object> getAllRequestWithoutUsersRequest(@RequestHeader("X-Sharer-User-Id") Long userId) {

        return itemRequestClient.getAllRequestWithoutUsersRequest(userId);
    }

    @GetMapping
    public ResponseEntity<Object> getItemRequestsByUserId(@RequestHeader("X-Sharer-User-Id") Long userId) {
        return itemRequestClient.getItemRequestsByUserId(userId);
    }


    @GetMapping("/{requestId}")
    public ResponseEntity<Object> getItemRequestsById(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                      @PathVariable Long requestId) {
        return itemRequestClient.getItemRequestsById(userId, requestId);
    }
}
