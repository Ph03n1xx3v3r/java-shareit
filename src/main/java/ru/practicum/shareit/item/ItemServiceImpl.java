package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.BookingMapper;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;

    @Override
    @Transactional
    public ItemDto create(long userId, ItemDto itemDto) {
        validateForCreate(itemDto);
        User owner = getUser(userId);
        Item item = ItemMapper.toItem(itemDto);
        item.setOwner(owner);
        return buildDto(itemRepository.save(item), false);
    }

    @Override
    @Transactional
    public ItemDto update(long userId, long itemId, ItemDto itemDto) {
        getUser(userId);
        Item item = getItem(itemId);

        if (!item.getOwner().getId().equals(userId)) {
            throw new NotFoundException("Пользователь с id=" + userId + " не является владельцем вещи");
        }

        if (itemDto.getName() != null) {
            validateText(itemDto.getName(), "Название вещи не может быть пустым");
            item.setName(itemDto.getName());
        }
        if (itemDto.getDescription() != null) {
            validateText(itemDto.getDescription(), "Описание вещи не может быть пустым");
            item.setDescription(itemDto.getDescription());
        }
        if (itemDto.getAvailable() != null) {
            item.setAvailable(itemDto.getAvailable());
        }

        return buildDto(itemRepository.save(item), false);
    }

    @Override
    public ItemDto getById(long itemId) {
        return buildDto(getItem(itemId), false);
    }

    @Override
    public List<ItemDto> getAllByOwner(long userId) {
        getUser(userId);
        return itemRepository.findAllByOwnerIdOrderByIdAsc(userId).stream()
                .map(item -> buildDto(item, true))
                .toList();
    }

    @Override
    public List<ItemDto> search(String text) {
        if (text == null || text.isBlank()) {
            return List.of();
        }
        return itemRepository.searchAvailable(text).stream()
                .map(item -> buildDto(item, false))
                .toList();
    }

    @Override
    @Transactional
    public CommentDto addComment(long userId, long itemId, CommentDto commentDto) {
        User author = getUser(userId);
        Item item = getItem(itemId);
        validateText(commentDto.getText(), "Текст комментария не может быть пустым");

        boolean completedBooking = bookingRepository.existsByItemIdAndBookerIdAndStatusAndEndBefore(
                itemId, userId, BookingStatus.APPROVED, LocalDateTime.now());
        if (!completedBooking) {
            throw new ValidationException("Оставить отзыв можно только после завершённого бронирования");
        }

        Comment comment = Comment.builder()
                .text(commentDto.getText())
                .item(item)
                .author(author)
                .created(LocalDateTime.now())
                .build();
        return CommentMapper.toDto(commentRepository.save(comment));
    }

    private ItemDto buildDto(Item item, boolean includeBookings) {
        ItemDto dto = ItemMapper.toItemDto(item);
        LocalDateTime now = LocalDateTime.now();
        if (includeBookings) {
            dto.setLastBooking(bookingRepository
                    .findFirstByItemIdAndStatusAndEndBeforeOrderByEndDesc(item.getId(), BookingStatus.APPROVED, now)
                    .map(BookingMapper::toShortDto)
                    .orElse(null));
            dto.setNextBooking(bookingRepository
                    .findFirstByItemIdAndStatusAndStartAfterOrderByStartAsc(item.getId(), BookingStatus.APPROVED, now)
                    .map(BookingMapper::toShortDto)
                    .orElse(null));
        }
        dto.setComments(commentRepository.findAllByItemIdOrderByCreatedAsc(item.getId()).stream()
                .map(CommentMapper::toDto)
                .toList());
        return dto;
    }

    private User getUser(long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id=" + userId + " не найден"));
    }

    private Item getItem(long itemId) {
        return itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Вещь с id=" + itemId + " не найдена"));
    }

    private void validateForCreate(ItemDto itemDto) {
        validateText(itemDto.getName(), "Название вещи не может быть пустым");
        validateText(itemDto.getDescription(), "Описание вещи не может быть пустым");
        if (itemDto.getAvailable() == null) {
            throw new ValidationException("Необходимо указать доступность вещи");
        }
    }

    private void validateText(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new ValidationException(message);
        }
    }
}
