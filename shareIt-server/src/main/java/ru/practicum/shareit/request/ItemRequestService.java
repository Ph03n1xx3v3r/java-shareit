package ru.practicum.shareit.request;

import ru.practicum.shareit.request.dto.ItemRequestDto;

import java.util.List;

public interface ItemRequestService {
    ItemRequestDto create(long userId, ItemRequestDto dto);
    List<ItemRequestDto> getOwnRequests(long userId);
    List<ItemRequestDto> getOtherRequests(long userId);
    ItemRequestDto getById(long requestId);
}
