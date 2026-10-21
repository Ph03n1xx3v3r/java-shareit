package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.dto.ItemRequestAnswerDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemRequestServiceImpl implements ItemRequestService {
    private final ItemRequestRepository requestRepository;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public ItemRequestDto create(long userId, ItemRequestDto dto) {
        if (dto == null || dto.getDescription() == null || dto.getDescription().isBlank()) {
            throw new ValidationException("Описание запроса не может быть пустым");
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id=" + userId + " не найден"));
        ItemRequest request = ItemRequest.builder()
                .description(dto.getDescription().trim())
                .requestor(user)
                .created(LocalDateTime.now())
                .build();
        return toDto(requestRepository.save(request));
    }

    @Override
    public List<ItemRequestDto> getOwnRequests(long userId) {
        ensureUserExists(userId);
        return requestRepository.findAllByRequestorIdOrderByCreatedDesc(userId).stream().map(this::toDto).toList();
    }

    @Override
    public List<ItemRequestDto> getOtherRequests(long userId) {
        ensureUserExists(userId);
        return requestRepository.findAllByRequestorIdNotOrderByCreatedDesc(userId).stream().map(this::toDto).toList();
    }

    @Override
    public ItemRequestDto getById(long requestId) {
        ItemRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Запрос с id=" + requestId + " не найден"));
        return toDto(request);
    }

    private void ensureUserExists(long userId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("Пользователь с id=" + userId + " не найден");
        }
    }

    private ItemRequestDto toDto(ItemRequest request) {
        List<ItemRequestAnswerDto> answers = itemRepository.findAllByRequestIdOrderByIdAsc(request.getId()).stream()
                .map(this::toAnswerDto).toList();
        return ItemRequestDto.builder()
                .id(request.getId())
                .description(request.getDescription())
                .created(request.getCreated())
                .items(answers)
                .build();
    }

    private ItemRequestAnswerDto toAnswerDto(Item item) {
        return ItemRequestAnswerDto.builder()
                .id(item.getId()).name(item.getName()).ownerId(item.getOwner().getId()).build();
    }
}
