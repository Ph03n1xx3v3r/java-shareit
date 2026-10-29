package ru.practicum.shareit.request;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ItemRequestServiceImplTest {
    @Mock
    private ItemRequestRepository requestRepository;
    @Mock
    private ItemRepository itemRepository;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ItemRequestServiceImpl service;

    @Test
    void listOwnAndOtherRequests() {
        User user = User.builder().id(1L).name("Test user").email("test@example.com").build();
        ItemRequest ownRequest = ItemRequest.builder()
                .id(10L)
                .description("Need a drill")
                .requestor(user)
                .created(LocalDateTime.now())
                .build();
        ItemRequest otherRequest = ItemRequest.builder()
                .id(11L)
                .description("Need a bicycle")
                .requestor(User.builder().id(2L).name("Other user").email("other@example.com").build())
                .created(LocalDateTime.now().minusMinutes(1))
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(requestRepository.findAllByRequestorIdOrderByCreatedDesc(1L)).thenReturn(List.of(ownRequest));
        when(requestRepository.findAllByRequestorIdNotOrderByCreatedDesc(1L)).thenReturn(List.of(otherRequest));
        when(itemRepository.findAllByRequestIdOrderByIdAsc(10L)).thenReturn(List.of());
        when(itemRepository.findAllByRequestIdOrderByIdAsc(11L)).thenReturn(List.of());

        var ownRequests = service.getOwnRequests(1L);
        var otherRequests = service.getOtherRequests(1L);

        assertEquals(1, ownRequests.size());
        assertEquals("Need a drill", ownRequests.get(0).getDescription());
        assertEquals(1, otherRequests.size());
        assertEquals("Need a bicycle", otherRequests.get(0).getDescription());

        verify(requestRepository).findAllByRequestorIdOrderByCreatedDesc(1L);
        verify(requestRepository).findAllByRequestorIdNotOrderByCreatedDesc(1L);
    }

    @Test
    void missingUserForLists() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.getOwnRequests(1L));
        assertThrows(NotFoundException.class, () -> service.getOtherRequests(1L));
    }

    @Test
    void missingRequestForGetById() {
        assertThrows(NotFoundException.class, () -> service.getById(404L));
    }
}
