package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.NewBookingDto;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookingServiceImpl implements BookingService {
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    @Override
    @Transactional
    public BookingDto create(long userId, NewBookingDto dto) {
        User booker = getUser(userId);
        validateDateOrder(dto);
        Item item = itemRepository.findById(dto.getItemId())
                .orElseThrow(() -> new NotFoundException("Вещь с id=" + dto.getItemId() + " не найдена"));

        if (!Boolean.TRUE.equals(item.getAvailable())) {
            throw new ValidationException("Вещь недоступна для бронирования");
        }
        if (item.getOwner().getId().equals(userId)) {
            throw new NotFoundException("Владелец не может бронировать собственную вещь");
        }

        Booking booking = Booking.builder()
                .start(dto.getStart())
                .end(dto.getEnd())
                .item(item)
                .booker(booker)
                .status(BookingStatus.WAITING)
                .build();
        return BookingMapper.toDto(bookingRepository.save(booking));
    }

    @Override
    @Transactional
    public BookingDto approve(long userId, long bookingId, boolean approved) {
        Booking booking = getBooking(bookingId);
        if (!userRepository.existsById(userId)) {
            throw new ValidationException("Пользователь с id=" + userId + " не найден");
        }
        if (!booking.getItem().getOwner().getId().equals(userId)) {
            throw new ValidationException("Подтвердить бронирование может только владелец вещи");
        }
        if (booking.getStatus() != BookingStatus.WAITING) {
            throw new ValidationException("Бронирование уже обработано");
        }

        booking.setStatus(approved ? BookingStatus.APPROVED : BookingStatus.REJECTED);
        return BookingMapper.toDto(bookingRepository.save(booking));
    }

    @Override
    public BookingDto getById(long userId, long bookingId) {
        getUser(userId);
        Booking booking = getBooking(bookingId);
        boolean booker = booking.getBooker().getId().equals(userId);
        boolean owner = booking.getItem().getOwner().getId().equals(userId);
        if (!booker && !owner) {
            throw new NotFoundException("Нет доступа к бронированию id=" + bookingId);
        }
        return BookingMapper.toDto(booking);
    }

    @Override
    public List<BookingDto> getByBooker(long userId, String state) {
        getUser(userId);
        BookingState bookingState = BookingState.from(state);
        return findBookerBookings(userId, bookingState).stream()
                .map(BookingMapper::toDto)
                .toList();
    }

    @Override
    public List<BookingDto> getByOwner(long userId, String state) {
        getUser(userId);
        BookingState bookingState = BookingState.from(state);
        return findOwnerBookings(userId, bookingState).stream()
                .map(BookingMapper::toDto)
                .toList();
    }

    private List<Booking> findBookerBookings(long userId, BookingState state) {
        LocalDateTime now = LocalDateTime.now();
        Sort sort = Sort.by(Sort.Direction.DESC, "start");
        return switch (state) {
            case ALL -> bookingRepository.findAllByBookerId(userId, sort);
            case CURRENT -> bookingRepository
                    .findByBookerIdAndStartLessThanEqualAndEndGreaterThanEqual(userId, now, now, sort);
            case PAST -> bookingRepository.findByBookerIdAndEndBefore(userId, now, sort);
            case FUTURE -> bookingRepository.findByBookerIdAndStartAfter(userId, now, sort);
            case WAITING -> bookingRepository.findByBookerIdAndStatus(userId, BookingStatus.WAITING, sort);
            case REJECTED -> bookingRepository.findByBookerIdAndStatus(userId, BookingStatus.REJECTED, sort);
        };
    }

    private List<Booking> findOwnerBookings(long userId, BookingState state) {
        LocalDateTime now = LocalDateTime.now();
        Sort sort = Sort.by(Sort.Direction.DESC, "start");
        return switch (state) {
            case ALL -> bookingRepository.findAllByItemOwnerId(userId, sort);
            case CURRENT -> bookingRepository
                    .findByItemOwnerIdAndStartLessThanEqualAndEndGreaterThanEqual(userId, now, now, sort);
            case PAST -> bookingRepository.findByItemOwnerIdAndEndBefore(userId, now, sort);
            case FUTURE -> bookingRepository.findByItemOwnerIdAndStartAfter(userId, now, sort);
            case WAITING -> bookingRepository.findByItemOwnerIdAndStatus(userId, BookingStatus.WAITING, sort);
            case REJECTED -> bookingRepository.findByItemOwnerIdAndStatus(userId, BookingStatus.REJECTED, sort);
        };
    }

    private void validateDateOrder(NewBookingDto dto) {
        if (!dto.getEnd().isAfter(dto.getStart())) {
            throw new ValidationException("Окончание бронирования должно быть позже начала");
        }
    }

    private User getUser(long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id=" + userId + " не найден"));
    }

    private Booking getBooking(long bookingId) {
        return bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Бронирование с id=" + bookingId + " не найдено"));
    }
}
