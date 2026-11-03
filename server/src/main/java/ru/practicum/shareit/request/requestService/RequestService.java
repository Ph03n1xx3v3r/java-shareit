package ru.practicum.shareit.request.requestService;

import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.RequestWithItemDto;

import java.util.Collection;
import java.util.List;

public interface RequestService {
    ItemRequestDto create(ItemRequestDto itemRequestDto, Long userId);

    Collection<ItemRequestDto> getAllRequestWithoutUsersRequest(Long userId);

    List<RequestWithItemDto> getItemRequestsByUserId(Long userId);

    RequestWithItemDto getItemRequestsById(Long userId, Long requestId);

}
