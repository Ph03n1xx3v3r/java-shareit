package ru.practicum.shareit.request.mapper;

import ru.practicum.shareit.item.dto.ItemForRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.RequestWithItemDto;
import ru.practicum.shareit.request.model.Request;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;

public class ItemRequestMapper {

    public static ItemRequestDto toItemRequestDto(Request request) {
        ItemRequestDto itemRequestDto = new ItemRequestDto();
        itemRequestDto.setDescription(request.getDescription());
        itemRequestDto.setId(request.getId());
        itemRequestDto.setCreated(request.getCreated());
        return itemRequestDto;
    }

    public static Request toRequest(ItemRequestDto itemRequestDto, User user, LocalDateTime localDateTime) {
        Request request = new Request();
        request.setDescription(itemRequestDto.getDescription());
        request.setUser(user);
        request.setCreated(localDateTime);
        return request;
    }

    public static RequestWithItemDto toRequestWithItemDto(List<ItemForRequestDto> itemList, Request request) {
        RequestWithItemDto requestWithItemDto = new RequestWithItemDto();
        requestWithItemDto.setItems(itemList);
        requestWithItemDto.setId(request.getId());
        requestWithItemDto.setDescription(request.getDescription());
        requestWithItemDto.setCreated(request.getCreated());
        return requestWithItemDto;
    }
}
