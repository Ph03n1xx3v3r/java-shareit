package ru.practicum.shareit.item.mapper;

import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.item.dto.*;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.model.Request;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;

@Component
@NoArgsConstructor
public class ItemMapper {
    public static ItemDto toItemDto(Item item) {
        ItemDto itemDto = new ItemDto();
        itemDto.setId(item.getId());
        itemDto.setName(item.getName());
        itemDto.setDescription(item.getDescription());
        itemDto.setAvailable(item.getAvailable());
        if (item.getRequest() != null) {
            itemDto.setRequestId(item.getRequest().getId());
        }
        return itemDto;
    }

    public static Item toItem(User user, ItemDto itemDto) {
        Item item = new Item();
        item.setName(itemDto.getName());
        item.setDescription(itemDto.getDescription());
        item.setAvailable(itemDto.getAvailable());
        item.setUser(user);
        return item;
    }

    public static Item toItem(User user, ItemDto itemDto, Request request) {
        Item item = new Item();
        item.setName(itemDto.getName());
        item.setDescription(itemDto.getDescription());
        item.setAvailable(itemDto.getAvailable());
        item.setUser(user);
        item.setRequest(request);
        return item;
    }

    public static ItemSpecificationDto toItemSpecificationDto(Item item) {
        ItemSpecificationDto itemSpecificationDto = new ItemSpecificationDto();
        itemSpecificationDto.setName(item.getName());
        itemSpecificationDto.setDescription(item.getDescription());
        return itemSpecificationDto;
    }

    public static ItemWithBookingDto toItemWithBookingDto(Item item, LocalDateTime lastBooking,
                                                          LocalDateTime nextBooking,
                                                          List<CommentDto> comment) {
        ItemWithBookingDto itemWithBookingDto = new ItemWithBookingDto();
        itemWithBookingDto.setId(item.getId());
        itemWithBookingDto.setComments(comment);
        itemWithBookingDto.setName(item.getName());
        itemWithBookingDto.setDescription(item.getDescription());
        itemWithBookingDto.setLastBooking(lastBooking);
        itemWithBookingDto.setNextBooking(nextBooking);
        itemWithBookingDto.setAvailable(item.getAvailable());
        return itemWithBookingDto;
    }

    public static ItemListDto toItemListDto(Item item, LocalDateTime lastBooking,
                                            LocalDateTime nextBooking,
                                            List<CommentDto> commentDtoList) {
        ItemListDto itemListDto = new ItemListDto();
        itemListDto.setId(item.getId());
        itemListDto.setName(item.getName());
        itemListDto.setDescription(item.getDescription());
        itemListDto.setLastBooking(lastBooking);
        itemListDto.setNextBooking(nextBooking);
        itemListDto.setComments(commentDtoList);
        return itemListDto;
    }

    public static ItemForRequestDto toItemForRequestDto(Item item, Long userid) {
        ItemForRequestDto itemForRequestDto = new ItemForRequestDto();
        itemForRequestDto.setId(item.getId());
        itemForRequestDto.setName(item.getName());
        itemForRequestDto.setOwnerId(userid);
        return itemForRequestDto;
    }
}
