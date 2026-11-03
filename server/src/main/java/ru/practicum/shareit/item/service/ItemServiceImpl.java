package ru.practicum.shareit.item.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.StatusBooking;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.ConditionsNotMetException;
import ru.practicum.shareit.exception.ObjectNotFoundException;
import ru.practicum.shareit.item.dto.*;
import ru.practicum.shareit.item.mapper.CommentMapper;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.model.Request;
import ru.practicum.shareit.request.reposirtory.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ItemServiceImpl implements ItemService {

    UserService userService;
    BookingRepository bookingRepository;
    UserRepository userRepository;
    ItemRepository itemRepository;
    CommentRepository commentRepository;
    ItemRequestRepository itemRequestRepository;

    public ItemServiceImpl(@Qualifier("userServiceImpl") UserService userService,
                           @Qualifier("itemRepository") ItemRepository itemRepository,
                           @Qualifier("bookingRepository") BookingRepository bookingRepository,
                           @Qualifier("commentRepository") CommentRepository commentRepository,
                           @Qualifier("userRepository") UserRepository userRepository,
                           @Qualifier("itemRequestRepository") ItemRequestRepository itemRequestRepository
    ) {
        this.userService = userService;
        this.itemRepository = itemRepository;
        this.bookingRepository = bookingRepository;
        this.commentRepository = commentRepository;
        this.userRepository = userRepository;
        this.itemRequestRepository = itemRequestRepository;
    }

    @Override
    public ItemDto createItem(Long userId, ItemDto itemDto) {
        log.info("Получили запрос на добавление новой вещи - " + itemDto + "\n от пользователя c ID - " + userId);
        log.info("Проверяем ID пользоваеля");
        User user = getUserEntityById(userId);
        normalizeField(itemDto);
        Request request;
        Item item;
        if (itemDto.getRequestId() != null) {
            request = getRequestEntityById(itemDto.getRequestId());
            item = ItemMapper.toItem(user, itemDto, request);
        } else {
            item = ItemMapper.toItem(user, itemDto);
        }
        Item newItem = itemRepository.save(item);
        return ItemMapper.toItemDto(newItem);
    }

    @Override
    public ItemDto updateItem(Long userId, Map<String, String> updatesItem, Long itemId) {
        log.info("Получили запрос на редактирование вещи с ID - " + itemId + "\n от пользователя c ID - " + userId);
        log.info("Проверяем ID пользователя");
        userService.getUserById(userId);
        log.info("Проверяем ID вещи и его владельца");
        checkItem(itemId, userId);
        log.info("Проверяем тело запроса");
        checkUpdatesItemMap(updatesItem);
        log.info("Обновление данных");
        return ItemMapper.toItemDto(update(updatesItem, itemId));
    }

    @Override
    public ItemWithBookingDto getItemById(Long itemId) {
        log.info("Получили запрос на получении вещи с Id - " + itemId);
        Optional<Item> item = itemRepository.findById(itemId);
        if (itemId < 0 || item.isEmpty()) {
            log.warn("Вещи по указанному id не существует - " + itemId);
            throw new ObjectNotFoundException("Вещи по указанному id не существует или некорректный id - " + itemId);
        }
        List<CommentDto> commentListForItem = getCommetListForItem(itemId)
                .stream()
                .map(comment -> CommentMapper.toCommentDto(comment, comment.getUser()))
                .collect(Collectors.toList());
        List<Booking> listBooking = bookingRepository.findBookingByItemAndLocalDateTime(itemId, LocalDateTime.now());
        if (listBooking.size() == 0) {
            return ItemMapper.toItemWithBookingDto(item.get(), null,
                    null, commentListForItem);
        } else if (listBooking.size() == 1) {
            return ItemMapper.toItemWithBookingDto(item.get(), listBooking.get(0).getStart(),
                    null, commentListForItem);
        }
        return ItemMapper.toItemWithBookingDto(item.get(), listBooking.get(0).getStart(),
                listBooking.get(1).getStart(), commentListForItem);
    }

    public Item getItemEntityById(Long itemId) {
        log.info("Получили запрос на получении вещи с Id - " + itemId);
        Optional<Item> item = itemRepository.findById(itemId);
        if (itemId < 0 || item.isEmpty()) {
            log.warn("Вещи по указанному id не существует - " + itemId);
            throw new ObjectNotFoundException("Вещи по указанному id не существует или некорректный id - " + itemId);
        }
        return item.get();
    }

    @Override
    public Collection<ItemListDto> getAllItemsFromUser(Long userId) {
        log.info("Получили запрос на получении всех вещей для пользователя с Id - " + userId);
        userService.getUserById(userId);
        List<Item> itemList = itemRepository.getListItemForUser(userId); // Собрали вещи

        List<Long> itemIds = itemList.stream()
                .map((el) -> el.getId())
                .collect(Collectors.toList()); // Собрали айдишники вещей

        Map<Long, List<Booking>> itemBookingMap = getItemBookingMap(itemIds); // Сопоставляем лист броней с вещами

        Map<Long, List<Comment>> itemCommentMap = getItemCommentMap(itemIds); // Сопоставляем лист комментарий с вещами

        List<ItemListDto> itemListDto = getItemListDto(itemList, itemBookingMap, itemCommentMap); // Собираем ItemListDto

        return itemListDto;

    }

    @Override
    public Collection<ItemSpecificationDto> searchItemsForUser(Long userId, String text) {
        log.info("Получили запрос от пользователя с Id - " + userId +
                " на получении всех вещей содержащих строку - " + text);
        userService.getUserById(userId);
        return itemRepository.search(text).stream()
                .filter(item -> item.getAvailable() == true &&
                        (item.getName().toLowerCase().contains(text.toLowerCase())
                                || item.getDescription().toLowerCase().contains(text.toLowerCase())))
                .map((item) -> ItemMapper.toItemSpecificationDto(item))
                .collect(Collectors.toList());
    }

    @Override
    public CommentDto addCommentForItem(Long userId, Long itemId, CommentDto commentDto) {
        log.info("Получили запрос на добавление комментария - " + commentDto);
        User user = getUserEntityById(userId);
        Item item = getItemEntityById(itemId);
        checkBookingItem(user, item);
        Comment comment = CommentMapper.toComment(commentDto, user, item);
        return CommentMapper.toCommentDto(commentRepository.save(comment), user);
    }

    private Map<Long, List<Booking>> getItemBookingMap(List<Long> itemIds) {
        List<Booking> bookingListForItems = bookingRepository
                .findAllByItemIdsAndStatus(itemIds, StatusBooking.APPROVED); // Собрали брони для всех вещей
        Map<Long, List<Booking>> itemBookingMap = new HashMap<>(); // Сопоставляем лист броней с вещами
        for (Booking booking : bookingListForItems) {
            Long bookingId = booking.getItem().getId();
            if (!itemBookingMap.containsKey(bookingId)) {
                itemBookingMap.put(bookingId, new ArrayList<>());
                itemBookingMap.get(bookingId).add(booking);
            } else {
                itemBookingMap.get(bookingId).add(booking);
            }
        }
        return itemBookingMap;
    }

    private Map<Long, List<Comment>> getItemCommentMap(List<Long> itemIds) {
        List<Comment> commentListForItems = commentRepository
                .getCommetListForItemIds(itemIds); // Собрали комментарии для всех вещей

        Map<Long, List<Comment>> itemCommentMap = new HashMap<>(); // Сопоставляем лист комментарий с вещами
        for (Comment comment : commentListForItems) {
            Long commentId = comment.getItem().getId();
            if (!itemCommentMap.containsKey(commentId)) {
                itemCommentMap.put(commentId, new ArrayList<>());
                itemCommentMap.get(commentId).add(comment);
            } else {
                itemCommentMap.get(commentId).add(comment);
            }
        }
        return itemCommentMap;
    }

    private List<ItemListDto> getItemListDto(List<Item> itemList,
                                             Map<Long, List<Booking>> itemBookingMap,
                                             Map<Long, List<Comment>> itemCommentMap) {
        List<ItemListDto> listItemListDto = new ArrayList<>();

        for (Item item : itemList) {
            LocalDateTime lastBooking;
            LocalDateTime nextBooking;
            List<Booking> bookingList = itemBookingMap.get(item.getId());
            if (bookingList == null) {
                lastBooking = null;
                nextBooking = null;
            } else if (bookingList.size() == 1) {
                lastBooking = bookingList.get(0).getStart();
                nextBooking = null;
            } else {
                lastBooking = bookingList.get(0).getStart();
                nextBooking = bookingList.get(1).getStart();
            }
            List<CommentDto> commentDtoList;
            if (itemCommentMap.get(item.getId()) == null) {
                commentDtoList = new ArrayList<>();
            } else {
                commentDtoList = itemCommentMap.get(item.getId())
                        .stream()
                        .map((comment) -> CommentMapper.toCommentDto(comment, comment.getUser()))
                        .collect(Collectors.toList());
            }
            listItemListDto.add(ItemMapper.toItemListDto(item, lastBooking, nextBooking, commentDtoList));
        }

        return listItemListDto;
    }

    private List<Comment> getCommetListForItem(Long itemId) {
        return commentRepository.getCommetListForItem(itemId);
    }

    private void checkBookingItem(User user, Item item) {
        if (bookingRepository.findBookingByItemAndUser(user.getId(), item.getId(), LocalDateTime.now(),
                StatusBooking.APPROVED).isEmpty()) {
            log.warn("Не корректные входные данные");
            throw new ConditionsNotMetException("Не выполнены условия для комментария");
        }
    }

    private void normalizeField(ItemDto itemDto) {
        itemDto.setDescription(itemDto.getDescription().trim());
        itemDto.setName(itemDto.getName().trim());
    }

    private void checkItem(Long itemId, Long userId) {
        Item item = itemRepository.findById(itemId).get();
        if (itemRepository.findById(itemId).isEmpty() || !item.getUser().getId().equals(userId)) {
            log.warn("Не корректные входные данные");
            throw new ObjectNotFoundException("Пользователя по указанному id не существует или некорректный id");
        }
    }

    private void checkUpdatesItemMap(Map<String, String> updatesItem) {
        if (updatesItem.size() > 3) {
            log.warn("Не корректные входные данные");
            throw new ConditionsNotMetException("Не корректное тело запроса");
        }
        for (String s : updatesItem.keySet()) {
            if (!s.equals("name") && !s.equals("description") && !s.equals("available")) {
                log.warn("Не корректные входные данные");
                throw new ConditionsNotMetException("Не корректное тело запроса");
            }
            if (updatesItem.get(s).isBlank()) {
                log.warn("Не корректные входные данные");
                throw new ConditionsNotMetException("Не корректное тело запроса");
            }
        }
    }

    private Item update(Map<String, String> updatesItem, Long itemId) {
        Item item = itemRepository.findById(itemId).get();
        for (String s : updatesItem.keySet()) {
            if (s.equals("name") && updatesItem.get(s) != null
                    && !updatesItem.get(s).isBlank()) {
                item.setName(updatesItem.get(s).trim());
            } else if (s.equals("description") && updatesItem.get(s) != null
                    && !updatesItem.get(s).isBlank()
                    && updatesItem.get(s).length() <= 200) {
                item.setDescription(updatesItem.get(s).trim());
            } else if (s.equals("available")) {
                item.setAvailable(Boolean.parseBoolean(updatesItem.get(s)));
            } else {
                log.warn("Не корректные входные данные");
                throw new ConditionsNotMetException("Не корректное тело запроса");
            }
        }
        return itemRepository.save(item);
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

    public Request getRequestEntityById(Long id) {
        log.info("Получили запрос на получение запроса");
        log.info("Проверяем ID запроса");
        checkRequestId(id);
        Optional<Request> request = itemRequestRepository.findById(id);
        if (request.isEmpty()) {
            log.warn("Запрос на вещь по указаному id не существует - " + id);
            throw new ObjectNotFoundException("Запрос на вещь по указаному id не существует - " + id);
        }
        log.info("Передали запрос на вещь по ID-" + id);
        return request.get();
    }

    private void checkRequestId(Long id) {
        if (id == null || id < 0) {
            log.warn("Запрос на вещь по указаному id не существует - " + id);
            throw new ObjectNotFoundException("Запрос на вещь по указаному id не существует - " + id);
        }
    }

}