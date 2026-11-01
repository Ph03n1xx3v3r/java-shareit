package ru.practicum.shareit.request;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.shareit.request.dto.ItemRequestDto;

import java.util.List;

@RestController
@RequestMapping(path = "/requests")
@RequiredArgsConstructor
public class ItemRequestController {
    private static final String USER_ID_HEADER = "X-Sharer-User-Id";
    private final ItemRequestService requestService;

    @PostMapping
    public ItemRequestDto create(@RequestHeader(USER_ID_HEADER) long userId,
                                 @Valid @RequestBody ItemRequestDto dto) {
        return requestService.create(userId, dto);
    }

    @GetMapping
    public List<ItemRequestDto> getOwn(@RequestHeader(USER_ID_HEADER) long userId,
                                       @RequestParam(defaultValue = "0") int from,
                                       @RequestParam(defaultValue = "10") int size) {
        return requestService.getOwn(userId, from, size);
    }

    // Convenience overloads for callers that use default pagination.
    public List<ItemRequestDto> getOwn(long userId) {
        return requestService.getOwnRequests(userId);
    }

    public List<ItemRequestDto> getOthers(long userId) {
        return requestService.getOtherRequests(userId);
    }

    @GetMapping("/all")
    public List<ItemRequestDto> getAllOther(@RequestHeader(USER_ID_HEADER) long userId,
                                            @RequestParam(defaultValue = "0") int from,
                                            @RequestParam(defaultValue = "10") int size) {
        return requestService.getAllOther(userId, from, size);
    }

    @GetMapping("/{requestId}")
    public ItemRequestDto getById(@PathVariable long requestId) {
        return requestService.getById(requestId);
    }
}
