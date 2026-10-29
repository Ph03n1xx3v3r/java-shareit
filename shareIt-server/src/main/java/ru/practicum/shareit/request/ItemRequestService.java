package ru.practicum.shareit.request;

import ru.practicum.shareit.request.dto.ItemRequestDto;

import java.util.List;

public interface ItemRequestService {
    ItemRequestDto create(long userId, ItemRequestDto dto);
    List<ItemRequestDto> getOwn(long userId, int from, int size);
    List<ItemRequestDto> getAllOther(long userId, int from, int size);

    List<ItemRequestDto> getOwnRequests(long userId);
    List<ItemRequestDto> getOtherRequests(long userId);
    ItemRequestDto getById(long requestId);
}
