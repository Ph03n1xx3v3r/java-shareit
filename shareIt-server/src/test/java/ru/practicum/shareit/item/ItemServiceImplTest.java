package ru.practicum.shareit.item;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.ItemRequestRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ItemServiceImplTest {
    @Mock private ItemRepository itemRepository;
    @Mock private UserRepository userRepository;
    @Mock private BookingRepository bookingRepository;
    @Mock private CommentRepository commentRepository;
    @Mock private ItemRequestRepository itemRequestRepository;
    @InjectMocks private ItemServiceImpl service;
    private User owner;
    private Item item;

    @BeforeEach
    void setUp() {
        owner = User.builder().id(1L).name("Owner").email("owner@example.com").build();
        item = Item.builder().id(10L).name("Drill").description("Tool").available(true).owner(owner).build();
    }

    private ItemDto validDto() {
        return ItemDto.builder().name("Drill").description("Tool").available(true).build();
    }

    @Test
    void createItemWithoutRequest() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(owner));
        when(itemRepository.save(any(Item.class))).thenAnswer(invocation -> {
            Item saved = invocation.getArgument(0); saved.setId(10L); return saved;
        });
        when(commentRepository.findAllByItemIdOrderByCreatedAsc(10L)).thenReturn(List.of());
        assertThat(service.create(1L, validDto()).getName()).isEqualTo("Drill");
    }

    @Test
    void createItemWithRequestAndRejectInvalidInput() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(owner));
        when(itemRequestRepository.findById(7L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.create(1L, ItemDto.builder().name("Drill").description("Tool")
                .available(true).requestId(7L).build())).isInstanceOf(NotFoundException.class);
        assertThatThrownBy(() -> service.create(1L, ItemDto.builder().name(" ").description("Tool")
                .available(true).build())).isInstanceOf(ValidationException.class);
        assertThatThrownBy(() -> service.create(1L, ItemDto.builder().name("Drill").description(" ")
                .available(true).build())).isInstanceOf(ValidationException.class);
        assertThatThrownBy(() -> service.create(1L, ItemDto.builder().name("Drill").description("Tool")
                .build())).isInstanceOf(ValidationException.class);
    }

    @Test
    void updateChangesFieldsAndChecksOwner() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(owner));
        when(itemRepository.findById(10L)).thenReturn(Optional.of(item));
        when(itemRepository.save(any(Item.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(commentRepository.findAllByItemIdOrderByCreatedAsc(10L)).thenReturn(List.of());
        ItemDto updated = service.update(1L, 10L, ItemDto.builder().name("New drill")
                .description("New description").available(false).build());
        assertThat(updated.getName()).isEqualTo("New drill");
        assertThat(updated.getDescription()).isEqualTo("New description");
        assertThat(updated.getAvailable()).isFalse();
        assertThatThrownBy(() -> service.update(2L, 10L, validDto())).isInstanceOf(NotFoundException.class);
        assertThatThrownBy(() -> service.update(1L, 10L, ItemDto.builder().name(" ").build()))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void getByIdAndAllByOwner() {
        when(itemRepository.findById(10L)).thenReturn(Optional.of(item));
        when(userRepository.findById(1L)).thenReturn(Optional.of(owner));
        when(itemRepository.findAllByOwnerIdOrderByIdAsc(1L)).thenReturn(List.of(item));
        when(commentRepository.findAllByItemIdOrderByCreatedAsc(10L)).thenReturn(List.of());
        when(bookingRepository.findFirstByItemIdAndStatusAndEndBeforeOrderByEndDesc(any(), any(), any()))
                .thenReturn(Optional.empty());
        when(bookingRepository.findFirstByItemIdAndStatusAndStartAfterOrderByStartAsc(any(), any(), any()))
                .thenReturn(Optional.empty());
        assertThat(service.getById(10L).getId()).isEqualTo(10L);
        assertThat(service.getAllByOwner(1L)).hasSize(1);
        assertThatThrownBy(() -> service.getById(404L)).isInstanceOf(NotFoundException.class);
    }

    @Test
    void searchHandlesBlankAndResults() {
        assertThat(service.search(" ")).isEmpty();
        assertThat(service.search(null)).isEmpty();
        when(itemRepository.searchAvailable("drill")).thenReturn(List.of(item));
        when(commentRepository.findAllByItemIdOrderByCreatedAsc(10L)).thenReturn(List.of());
        assertThat(service.search("drill")).hasSize(1);
    }

    @Test
    void commentRequiresCompletedBooking() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(owner));
        when(itemRepository.findById(10L)).thenReturn(Optional.of(item));
        assertThatThrownBy(() -> service.addComment(1L, 10L, CommentDto.builder().text("Nice").build()))
                .isInstanceOf(ValidationException.class);
        when(bookingRepository.existsByItemIdAndBookerIdAndStatusAndEndBefore(any(), any(), any(), any()))
                .thenReturn(true);
        when(commentRepository.save(any(Comment.class))).thenAnswer(invocation -> {
            Comment comment = invocation.getArgument(0); comment.setId(4L); return comment;
        });
        assertThat(service.addComment(1L, 10L, CommentDto.builder().text("Nice").build()).getText())
                .isEqualTo("Nice");
        assertThatThrownBy(() -> service.addComment(1L, 10L, CommentDto.builder().text(" ").build()))
                .isInstanceOf(ValidationException.class);
    }
}
