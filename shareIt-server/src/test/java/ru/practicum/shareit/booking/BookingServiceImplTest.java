package ru.practicum.shareit.booking;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;
import ru.practicum.shareit.booking.dto.NewBookingDto;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookingServiceImplTest {
    @Mock private BookingRepository bookingRepository;
    @Mock private UserRepository userRepository;
    @Mock private ItemRepository itemRepository;
    @InjectMocks private BookingServiceImpl service;
    private User owner;
    private User booker;
    private Item item;

    @BeforeEach
    void setUp() {
        owner = User.builder().id(1L).name("Owner").email("owner@example.com").build();
        booker = User.builder().id(2L).name("Booker").email("booker@example.com").build();
        item = Item.builder().id(10L).name("Drill").description("Tool").available(true).owner(owner).build();
    }

    private NewBookingDto validDto() {
        return NewBookingDto.builder().itemId(10L).start(LocalDateTime.now().plusDays(2))
                .end(LocalDateTime.now().plusDays(3)).build();
    }

    @Test
    void createBooking() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(booker));
        when(itemRepository.findById(10L)).thenReturn(Optional.of(item));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> {
            Booking booking = invocation.getArgument(0); booking.setId(5L); return booking;
        });
        assertThat(service.create(2L, validDto()).getId()).isEqualTo(5L);
    }

    @Test
    void createRejectsMissingUserAndItem() {
        when(userRepository.findById(2L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.create(2L, validDto())).isInstanceOf(NotFoundException.class);
        when(userRepository.findById(2L)).thenReturn(Optional.of(booker));
        when(itemRepository.findById(10L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.create(2L, validDto())).isInstanceOf(NotFoundException.class);
    }

    @Test
    void createRejectsInvalidDatesUnavailableItemAndOwner() {
        NewBookingDto badDates = NewBookingDto.builder().itemId(10L).start(LocalDateTime.now().plusDays(3))
                .end(LocalDateTime.now().plusDays(2)).build();
        when(userRepository.findById(2L)).thenReturn(Optional.of(booker));
        assertThatThrownBy(() -> service.create(2L, badDates)).isInstanceOf(ValidationException.class);
        when(itemRepository.findById(10L)).thenReturn(Optional.of(Item.builder().id(10L).available(false)
                .owner(owner).build()));
        assertThatThrownBy(() -> service.create(2L, validDto())).isInstanceOf(ValidationException.class);
        when(itemRepository.findById(10L)).thenReturn(Optional.of(item));
        when(userRepository.findById(1L)).thenReturn(Optional.of(owner));
        assertThatThrownBy(() -> service.create(1L, validDto())).isInstanceOf(NotFoundException.class);
    }

    @Test
    void approveAndRejectBooking() {
        Booking booking = booking();
        when(bookingRepository.findById(5L)).thenReturn(Optional.of(booking));
        when(userRepository.existsById(1L)).thenReturn(true);
        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> invocation.getArgument(0));
        assertThat(service.approve(1L, 5L, true).getStatus()).isEqualTo(BookingStatus.APPROVED);
        booking.setStatus(BookingStatus.WAITING);
        assertThat(service.approve(1L, 5L, false).getStatus()).isEqualTo(BookingStatus.REJECTED);
    }

    @Test
    void approveRejectsWrongUserMissingOwnerAndProcessedBooking() {
        Booking booking = booking();
        when(bookingRepository.findById(5L)).thenReturn(Optional.of(booking));
        when(userRepository.existsById(1L)).thenReturn(false);
        assertThatThrownBy(() -> service.approve(1L, 5L, true)).isInstanceOf(ValidationException.class);
        when(userRepository.existsById(1L)).thenReturn(true);
        when(userRepository.existsById(2L)).thenReturn(true);
        assertThatThrownBy(() -> service.approve(2L, 5L, true)).isInstanceOf(ValidationException.class);
        booking.setStatus(BookingStatus.APPROVED);
        assertThatThrownBy(() -> service.approve(1L, 5L, true)).isInstanceOf(ValidationException.class);
    }

    @Test
    void getByIdAllowsBookerAndOwnerButNotOthers() {
        Booking booking = booking();
        when(userRepository.findById(2L)).thenReturn(Optional.of(booker));
        when(userRepository.findById(1L)).thenReturn(Optional.of(owner));
        when(bookingRepository.findById(5L)).thenReturn(Optional.of(booking));
        assertThat(service.getById(2L, 5L).getId()).isEqualTo(5L);
        assertThat(service.getById(1L, 5L).getId()).isEqualTo(5L);
        when(userRepository.findById(3L)).thenReturn(Optional.of(User.builder().id(3L).build()));
        assertThatThrownBy(() -> service.getById(3L, 5L)).isInstanceOf(NotFoundException.class);
    }

    @Test
    void bookingQueriesCoverAllStatesForBookerAndOwner() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(booker));
        when(userRepository.findById(1L)).thenReturn(Optional.of(owner));
        when(bookingRepository.findAllByBookerId(any(Long.class), any(Sort.class))).thenReturn(List.of());
        when(bookingRepository.findAllByItemOwnerId(any(Long.class), any(Sort.class))).thenReturn(List.of());
        when(bookingRepository.findByBookerIdAndStartLessThanEqualAndEndGreaterThanEqual(
                any(), any(), any(), any())).thenReturn(List.of());
        when(bookingRepository.findByBookerIdAndEndBefore(any(), any(), any())).thenReturn(List.of());
        when(bookingRepository.findByBookerIdAndStartAfter(any(), any(), any())).thenReturn(List.of());
        when(bookingRepository.findByBookerIdAndStatus(any(), any(), any())).thenReturn(List.of());
        when(bookingRepository.findByItemOwnerIdAndStartLessThanEqualAndEndGreaterThanEqual(
                any(), any(), any(), any())).thenReturn(List.of());
        when(bookingRepository.findByItemOwnerIdAndEndBefore(any(), any(), any())).thenReturn(List.of());
        when(bookingRepository.findByItemOwnerIdAndStartAfter(any(), any(), any())).thenReturn(List.of());
        when(bookingRepository.findByItemOwnerIdAndStatus(any(), any(), any())).thenReturn(List.of());
        for (BookingState state : BookingState.values()) {
            assertThat(service.getByBooker(2L, state.name())).isEmpty();
            assertThat(service.getByOwner(1L, state.name())).isEmpty();
        }
        assertThat(service.getByBooker(2L, "all")).isEmpty();
        assertThatThrownBy(() -> service.getByBooker(2L, "bad")).isInstanceOf(ValidationException.class);
    }

    @Test
    void missingBookingThrows() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(booker));
        when(bookingRepository.findById(404L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.getById(2L, 404L)).isInstanceOf(NotFoundException.class);
    }

    private Booking booking() {
        return Booking.builder().id(5L).start(LocalDateTime.now().plusDays(2)).end(LocalDateTime.now().plusDays(3))
                .item(item).booker(booker).status(BookingStatus.WAITING).build();
    }
}
