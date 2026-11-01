package ru.practicum.shareit.request.requestService;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.ObjectNotFoundException;
import ru.practicum.shareit.item.dto.ItemForRequestDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.RequestWithItemDto;
import ru.practicum.shareit.request.mapper.ItemRequestMapper;
import ru.practicum.shareit.request.model.Request;
import ru.practicum.shareit.request.reposirtory.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class RequestServiceImpl implements RequestService {

    ItemRequestRepository itemRequestRepository;
    UserRepository userRepository;
    ItemRepository itemRepository;

    public RequestServiceImpl(@Qualifier("itemRequestRepository") ItemRequestRepository itemRequestRepository,
                              @Qualifier("userRepository") UserRepository userRepository,
                              @Qualifier("itemRepository") ItemRepository itemRepository) {
        this.itemRequestRepository = itemRequestRepository;
        this.userRepository = userRepository;
        this.itemRepository = itemRepository;
    }

    @Override
    public ItemRequestDto create(ItemRequestDto itemRequestDto, Long userId) {
        log.info("Получили запрос на добавление нового запроса на вещь - " + itemRequestDto + "\n от пользователя c ID - " + userId);
        log.info("Проверяем ID пользоваеля");
        User user = getUserEntityById(userId);
        log.info("Создам запрос");
        Request request = ItemRequestMapper.toRequest(itemRequestDto, user, LocalDateTime.now());
        Request newRequest = itemRequestRepository.save(request);
        return ItemRequestMapper.toItemRequestDto(newRequest);
    }

    @Override
    public Collection<ItemRequestDto> getAllRequestWithoutUsersRequest(Long userId) {
        log.info("Получили запрос на получение списока запросов, созданных другими пользователями");
        log.info("Проверяем ID пользоваеля");
        getUserEntityById(userId);
        log.info("Запрос в БД");
        List<Request> itemRequestList = itemRequestRepository.getAllRequestWithoutUsersRequest(userId);
        return itemRequestList.stream()
                .map((el) -> ItemRequestMapper.toItemRequestDto(el))
                .collect(Collectors.toList());
    }

    @Override
    public List<RequestWithItemDto> getItemRequestsByUserId(Long userId) {
        log.info("Получили запрос на получение списока запросов, созданных текущим пользователем");
        log.info("Проверяем ID пользоваеля");
        getUserEntityById(userId);
        log.info("Формируем ответ");
        List<Request> requestListForUser = getRequestListForUser(userId); // Получаем список всех запросов от пользователя
        List<Long> requestsIds = getRequestsIds(requestListForUser); // Получаем список id запросов
        List<Item> itemListForRequest = getItemListForRequest(requestsIds); // Получаем список ответов/вещей на запрос
        Map<Long, List<Item>> mapForRequest = getMapForRequest(requestsIds, itemListForRequest); // Сопоставляем ID запроса с вещами

        return getListItemRequestWithItem(requestListForUser, mapForRequest, userId);
    }

    @Override
    public RequestWithItemDto getItemRequestsById(Long userId, Long requestId) {
        log.info("Получили запрос на получение запроса по id - " + requestId);
        log.info("Проверяем ID пользоваеля");
        getUserEntityById(userId);
        log.info("Формируем ответ");
        Optional<Request> request = itemRequestRepository.findById(requestId);
        if (request.isEmpty()) {
            return new RequestWithItemDto();
        }

        List<Item> itemListForRequest = getItemListForRequest(List.of(requestId)); // Получаем список ответов/вещей на запрос
        List<ItemForRequestDto> itemForRequestDtoList =
                itemListForRequest.stream()
                        .map((el) -> ItemMapper.toItemForRequestDto(el, request.get().getUser().getId()))
                        .collect(Collectors.toList());
        return ItemRequestMapper.toRequestWithItemDto(itemForRequestDtoList, request.get());
    }

    private List<Request> getRequestListForUser(Long userId) {
        return itemRequestRepository.findAllRequestByUserId(userId);
    }

    private List<Long> getRequestsIds(List<Request> requestList) {
        return requestList.stream()
                .map((el) ->
                        el.getId())
                .collect(Collectors.toList());
    }

    private List<Item> getItemListForRequest(List<Long> requestsIds) {
        return itemRepository.getItemListForRequest(requestsIds);
    }

    private Map<Long, List<Item>> getMapForRequest(List<Long> requestsIds,
                                                   List<Item> itemListForRequest) {
        Map<Long, List<Item>> mapForRequest = new HashMap<>();
        for (Long requestsId : requestsIds) {
            mapForRequest.put(requestsId, itemListForRequest.stream()
                    .filter((el) -> el.getRequest().getId().equals(requestsId))
                    .collect(Collectors.toList()));
        }
        return mapForRequest;
    }

    private List<RequestWithItemDto> getListItemRequestWithItem(List<Request> requestListForUser,
                                                                Map<Long, List<Item>> mapForRequest,
                                                                Long userId) {
        return requestListForUser.stream()
                .map((el) ->
                        ItemRequestMapper.toRequestWithItemDto(
                                getItemForRequestDtoList(mapForRequest.get(el.getId()), userId),
                                el
                        )
                )
                .collect(Collectors.toList());
    }

    private List<ItemForRequestDto> getItemForRequestDtoList(List<Item> itemList, Long userId) {
        return itemList.stream()
                .map((el) ->
                        ItemMapper.toItemForRequestDto(el, userId))
                .collect(Collectors.toList());
    }

    private User getUserEntityById(Long id) {
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
