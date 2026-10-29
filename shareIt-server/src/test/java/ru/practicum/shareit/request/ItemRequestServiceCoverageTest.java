package ru.practicum.shareit.request;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ItemRequestServiceCoverageTest {
    @Mock
    private ItemRequestRepository requestRepository;
    @Mock
    private ItemRepository itemRepository;
    @Mock
    private UserRepository userRepository;
    @InjectMocks
    private ItemRequestServiceImpl service;

    @Test
    void createRejectsInvalidDescriptions() {
        assertThrows(ValidationException.class, () -> service.create(1L, null));
        assertThrows(ValidationException.class,
                () -> service.create(1L, ItemRequestDto.builder().description("  ").build()));
        assertThrows(ValidationException.class,
                () -> service.create(1L, ItemRequestDto.builder().description("x".repeat(1001)).build()));
    }

    @Test
    void createRejectsMissingUserAndCreatesRequest() {
        ItemRequestDto dto = ItemRequestDto.builder().description("Need a drill").build();
        when(userRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> service.create(1L, dto));

        User user = User.builder().id(1L).name("User").email("user@example.com").build();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(requestRepository.save(any(ItemRequest.class))).thenAnswer(invocation -> {
            ItemRequest request = invocation.getArgument(0);
            request.setId(5L);
            return request;
        });
        when(itemRepository.findAllByRequestIdOrderByIdAsc(5L)).thenReturn(List.of());

        ItemRequestDto result = service.create(1L, dto);

        assertEquals(5L, result.getId());
        assertEquals("Need a drill", result.getDescription());
        verify(requestRepository).save(any(ItemRequest.class));
    }

    @Test
    void paginatedListsValidateAndMapRequests() {
        User user = User.builder().id(1L).name("User").email("user@example.com").build();
        ItemRequest request = ItemRequest.builder().id(10L).description("Need a bike")
                .requestor(user).created(LocalDateTime.now()).build();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(requestRepository.findAllByRequestorIdOrderByCreatedDesc(anyLong(), any(Pageable.class)))
                .thenReturn(List.of(request));
        when(requestRepository.findAllByRequestorIdNotOrderByCreatedDesc(anyLong(), any(Pageable.class)))
                .thenReturn(List.of(request));
        when(itemRepository.findAllByRequestIdOrderByIdAsc(10L)).thenReturn(List.of());

        assertEquals(1, service.getOwn(1L, 0, 10).size());
        assertEquals(1, service.getAllOther(1L, 0, 10).size());
        assertThrows(ValidationException.class, () -> service.getOwn(1L, -1, 10));
        assertThrows(ValidationException.class, () -> service.getOwn(1L, 0, 0));
        assertThrows(ValidationException.class, () -> service.getAllOther(1L, -1, 10));
        assertThrows(ValidationException.class, () -> service.getAllOther(1L, 0, 0));
    }

    @Test
    void getByIdMapsAnswerItems() {
        User owner = User.builder().id(2L).name("Owner").email("owner@example.com").build();
        ItemRequest request = ItemRequest.builder().id(10L).description("Need a bike")
                .created(LocalDateTime.now()).build();
        Item item = Item.builder().id(20L).name("City bike").owner(owner).build();
        when(requestRepository.findById(10L)).thenReturn(Optional.of(request));
        when(itemRepository.findAllByRequestIdOrderByIdAsc(10L)).thenReturn(List.of(item));

        ItemRequestDto result = service.getById(10L);

        assertEquals(1, result.getItems().size());
        assertEquals(20L, result.getItems().get(0).getId());
        assertEquals("City bike", result.getItems().get(0).getName());
        assertEquals(2L, result.getItems().get(0).getOwnerId());
    }
}
