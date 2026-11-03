package ru.practicum.shareit.booking.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingSpecificationDto;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.State;
import ru.practicum.shareit.booking.model.StatusBooking;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.ConditionsNotMetException;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.exception.ObjectNotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemServiceImpl;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


@Service
@Slf4j
public class BookingServiceImpl implements BookingService {

    BookingRepository bookingRepository;
    UserRepository userRepository;
    UserService userService;
    ItemServiceImpl itemService;

    public BookingServiceImpl(@Qualifier("bookingRepository") BookingRepository bookingRepository,
                              @Qualifier("userServiceImpl") UserService userService,
                              @Qualifier("itemServiceImpl") ItemServiceImpl itemService,
                              @Qualifier("userRepository") UserRepository userRepository) {
        this.bookingRepository = bookingRepository;
        this.userService = userService;
        this.itemService = itemService;
        this.userRepository = userRepository;
    }

    @Override
    public BookingSpecificationDto createBooking(BookingDto bookingDto, Long userId) {
        log.info("Получили запрос на бронирование - " + bookingDto + " от пользователя " + userId);
        log.info("Поиск клиента в БД - " + userId);
        User user = getUserEntityById(userId);
        log.info("Поиск вещи в БД - " + bookingDto.getItemId());
        Item item = itemService.getItemEntityById(bookingDto.getItemId());
        log.info("Валидация статуса вещи");
        checkAvailableItem(item);
        log.info("Валидация даты и времени вещи");
        Booking booking = BookingMapper.toBooking(bookingDto, item, user);
        Booking newBooking = bookingRepository.save(booking);
        log.info("Успешно забронировали - " + booking);
        return BookingMapper.toBookingSpecificationDto(newBooking);
    }

    public Booking getBookingEntityById(Long bookingId) {
        log.info("Получили запрос на получении бронирования с Id - " + bookingId);
        Optional<Booking> booking = bookingRepository.findById(bookingId);
        if (booking.isEmpty()) {
            log.warn("Бронирования по указанному id не существует - " + bookingId);
            throw new ObjectNotFoundException("Бронирования по указанному id не существует или некорректный id - " + bookingId);
        }
        return booking.get();
    }

    @Override
    public BookingSpecificationDto getBookingById(Long bookingId, Long userId) {
        log.info("Получили запрос на получении бронирования с Id - " + bookingId);
        Optional<Booking> booking = bookingRepository.findById(bookingId);
        if (booking.isEmpty()) {
            log.warn("Бронирования по указанному id не существует - " + bookingId);
            throw new ObjectNotFoundException("Бронирования по указанному id не существует или некорректный id - " + bookingId);
        }
        checkObjectOwner(userId, booking.get());
        return BookingMapper.toBookingSpecificationDto(booking.get());
    }

    @Override
    public BookingSpecificationDto approveBooking(Long userId, Long bookingId, Boolean statusBooking) {
        log.info("Получили запрос на обновление статуса бронирования");
        userService.checkObjectOwnerAndGetUserEntityById(userId);
        Booking booking = getBookingEntityById(bookingId);
        checkBookingOwner(userId, booking);
        if (!booking.getStatus().equals(StatusBooking.WAITING)) {
            log.warn("Бронирование не находится в ожидании");
            throw new ConditionsNotMetException("Бронирование не находится в ожидании");
        }
        if (statusBooking) {
            booking.setStatus(StatusBooking.APPROVED);
            return BookingMapper.toBookingSpecificationDto(bookingRepository.save(booking));
        } else {
            booking.setStatus(StatusBooking.REJECTED);
            return BookingMapper.toBookingSpecificationDto(bookingRepository.save(booking));
        }
    }

    @Override
    public List<BookingSpecificationDto> getAllBookingByUser(Long userId, State state) {
        log.info("Получили запрос на получение списка всех бронирований текущего пользователя");
        userService.getUserById(userId);
        switch (state) {
            case ALL:
                return bookingRepository.findAllBookingByUserId(userId)
                        .stream()
                        .map(BookingMapper::toBookingSpecificationDto)
                        .collect(Collectors.toList());
            case PAST:
                return bookingRepository
                        .findPastBookingByUserId(userId, LocalDateTime.now(), StatusBooking.APPROVED)
                        .stream()
                        .map(BookingMapper::toBookingSpecificationDto)
                        .collect(Collectors.toList());
            case FUTURE:
                return bookingRepository
                        .findFutureBookingByUserId(userId, LocalDateTime.now(), StatusBooking.APPROVED)
                        .stream()
                        .map(BookingMapper::toBookingSpecificationDto)
                        .collect(Collectors.toList());
            case CURRENT:
                return bookingRepository
                        .findCurrentBookingByUserId(userId, LocalDateTime.now(), StatusBooking.APPROVED)
                        .stream()
                        .map(BookingMapper::toBookingSpecificationDto)
                        .collect(Collectors.toList());
            case WAITING:
                return bookingRepository
                        .findWaitingBookingByUserId(userId, StatusBooking.WAITING)
                        .stream()
                        .map(BookingMapper::toBookingSpecificationDto)
                        .collect(Collectors.toList());
            case REJECTED:
                return bookingRepository
                        .findRejectedBookingByUserId(userId, StatusBooking.REJECTED, StatusBooking.CANCELED)
                        .stream()
                        .map(BookingMapper::toBookingSpecificationDto)
                        .collect(Collectors.toList());
        }
        return new ArrayList<>();
    }

    @Override
    public List<BookingSpecificationDto> getAllBookingByItemOwner(Long ownerId, State state) {
        log.info("Получили запрос на получение списка всех бронирований текущего пользователя");
        userService.getUserById(ownerId);
        switch (state) {
            case ALL:
                return bookingRepository.findAllBookingByOwnerId(ownerId)
                        .stream()
                        .map(BookingMapper::toBookingSpecificationDto)
                        .collect(Collectors.toList());
            case PAST:
                return bookingRepository
                        .findPastBookingByOwnerId(ownerId, LocalDateTime.now(), StatusBooking.APPROVED)
                        .stream()
                        .map(BookingMapper::toBookingSpecificationDto)
                        .collect(Collectors.toList());
            case FUTURE:
                return bookingRepository
                        .findFutureBookingByOwnerId(ownerId, LocalDateTime.now(), StatusBooking.APPROVED)
                        .stream()
                        .map(BookingMapper::toBookingSpecificationDto)
                        .collect(Collectors.toList());
            case CURRENT:
                return bookingRepository
                        .findCurrentBookingByOwnerId(ownerId, LocalDateTime.now(), StatusBooking.APPROVED)
                        .stream()
                        .map(BookingMapper::toBookingSpecificationDto)
                        .collect(Collectors.toList());
            case WAITING:
                return bookingRepository
                        .findWaitingBookingByOwnerId(ownerId, StatusBooking.WAITING)
                        .stream()
                        .map(BookingMapper::toBookingSpecificationDto)
                        .collect(Collectors.toList());
            case REJECTED:
                return bookingRepository
                        .findRejectedBookingByOwnerId(ownerId, StatusBooking.REJECTED, StatusBooking.CANCELED)
                        .stream()
                        .map(BookingMapper::toBookingSpecificationDto)
                        .collect(Collectors.toList());
        }
        return new ArrayList<>();
    }

    private void checkObjectOwner(Long userId, Booking booking) {
        userService.getUserById(userId);
        if (!booking.getItem().getUser().getId().equals(userId) &&
                !booking.getUser().getId().equals(userId)) {
            log.warn("Доступ к данным запрещен для пользователя с id - " + userId);
            throw new ForbiddenException("Доступ к данным запрещен для пользователя с id - " + userId);
        }
    }

    private void checkBookingOwner(Long userId, Booking booking) {
        if (!booking.getItem().getUser().getId().equals(userId)) {
            log.warn("Вещь не принадлежит указанному пользователю с id - " + userId);
            throw new ObjectNotFoundException("Вещь не принадлежит указанному пользователю с id - " + userId);
        }
    }

    private void checkAvailableItem(Item item) {
        if (!item.getAvailable()) {
            log.warn("Вещь недоступна для бронирования");
            throw new ConditionsNotMetException("Вещь недоступна для бронирования");
        }
    }

    public User getUserEntityById(Long id) {
        log.info("Получили запрос на передачу пользоватля с ID-" + id);
        checkUserId(id);
        Optional<User> user = userRepository.findById(id);
        if (user.isEmpty()) {
            log.warn("Пользователя по указаному id не существует - " + id);
            throw new ObjectNotFoundException("Пользователя по указаному id не существует - " + id);
        }
        log.info("Передали пользователя по ID-" + id);
        return user.get();
    }

    private void checkUserId(Long id) {
        if (id == null || id < 0) {
            log.warn("Пользователя по указаному id не существует - " + id);
            throw new ObjectNotFoundException("Пользователя по указаному id не существует - " + id);
        }
    }

}
