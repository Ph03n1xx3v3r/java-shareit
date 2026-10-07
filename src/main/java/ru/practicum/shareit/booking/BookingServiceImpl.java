package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
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
        validateDates(dto);
        if (dto.getItemId() == null) {
            throw new ValidationException("Не указана вещь для бронирования");
        }

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
        return filter(bookingRepository.findAllByBookerIdOrderByStartDesc(userId), bookingState).stream()
                .map(BookingMapper::toDto)
                .toList();
    }

    @Override
    public List<BookingDto> getByOwner(long userId, String state) {
        getUser(userId);
        BookingState bookingState = BookingState.from(state);
        return filter(bookingRepository.findAllByItemOwnerIdOrderByStartDesc(userId), bookingState).stream()
                .map(BookingMapper::toDto)
                .toList();
    }

    private List<Booking> filter(List<Booking> bookings, BookingState state) {
        LocalDateTime now = LocalDateTime.now();
        return bookings.stream()
                .filter(booking -> switch (state) {
                    case ALL -> true;
                    case CURRENT -> !booking.getStart().isAfter(now) && !booking.getEnd().isBefore(now);
                    case PAST -> booking.getEnd().isBefore(now);
                    case FUTURE -> booking.getStart().isAfter(now);
                    case WAITING -> booking.getStatus() == BookingStatus.WAITING;
                    case REJECTED -> booking.getStatus() == BookingStatus.REJECTED;
                })
                .toList();
    }

    private void validateDates(NewBookingDto dto) {
        if (dto.getStart() == null || dto.getEnd() == null) {
            throw new ValidationException("Необходимо указать даты бронирования");
        }
        LocalDateTime now = LocalDateTime.now();
        if (!dto.getStart().isAfter(now)) {
            throw new ValidationException("Начало бронирования должно быть в будущем");
        }
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
