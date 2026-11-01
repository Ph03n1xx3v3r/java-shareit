package ru.practicum.shareit.request;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.RequestWithItemDto;
import ru.practicum.shareit.request.requestService.RequestService;

import java.util.Collection;
import java.util.List;

@RestController
@RequestMapping(path = "/requests")
public class ItemRequestController {

    RequestService requestService;

    public ItemRequestController(@Qualifier("requestServiceImpl") RequestService requestService) {
        this.requestService = requestService;
    }

    @PostMapping
    public ItemRequestDto create(@RequestBody ItemRequestDto itemRequestDto,
                                 @RequestHeader("X-Sharer-User-Id") Long userId) {

        return requestService.create(itemRequestDto, userId);
    }

    @GetMapping("/all")
    public Collection<ItemRequestDto> getAllRequestWithoutUsersRequest(@RequestHeader("X-Sharer-User-Id") Long userId) {

        return requestService.getAllRequestWithoutUsersRequest(userId);
    }

    @GetMapping
    public List<RequestWithItemDto> getItemRequestsByUserId(@RequestHeader("X-Sharer-User-Id") Long userId) {
        return requestService.getItemRequestsByUserId(userId);
    }


    @GetMapping("{requestId}")
    public RequestWithItemDto getItemRequestsById(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                  @PathVariable Long requestId) {
        return requestService.getItemRequestsById(userId, requestId);
    }
}
