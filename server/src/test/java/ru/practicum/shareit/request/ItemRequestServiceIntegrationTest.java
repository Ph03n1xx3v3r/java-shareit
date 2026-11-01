package ru.practicum.shareit.request;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ItemRequestServiceIntegrationTest {
    @Autowired
    private ItemRequestService requestService;
    @Autowired
    private ItemRequestRepository requestRepository;
    @Autowired
    private ItemRepository itemRepository;
    @Autowired
    private UserRepository userRepository;

    @Test
    void createsRequestAndReturnsItemsAddedAsAnswers() {
        User requestor = userRepository.save(User.builder().name("Requestor").email("requestor@example.com").build());
        User owner = userRepository.save(User.builder().name("Owner").email("owner@example.com").build());

        ItemRequestDto created = requestService.create(requestor.getId(),
                ItemRequestDto.builder().description("Need a drill").build());
        assertNotNull(created.getId());
        assertNotNull(created.getCreated());
        assertEquals("Need a drill", created.getDescription());
        assertEquals(0, created.getItems().size());

        ItemRequest request = requestRepository.findById(created.getId()).orElseThrow();
        Item item = itemRepository.save(Item.builder().name("Drill").description("Cordless drill")
                .available(true).owner(owner).request(request).build());

        ItemRequestDto result = requestService.getById(created.getId());
        assertEquals(1, result.getItems().size());
        assertEquals(item.getId(), result.getItems().get(0).getId());
        assertEquals("Drill", result.getItems().get(0).getName());
        assertEquals(owner.getId(), result.getItems().get(0).getOwnerId());
    }

    @Test
    void returnsOwnAndOtherRequestsNewestFirst() {
        User first = userRepository.save(User.builder().name("First").email("first@example.com").build());
        User second = userRepository.save(User.builder().name("Second").email("second@example.com").build());
        requestService.create(first.getId(), ItemRequestDto.builder().description("First request").build());
        requestService.create(second.getId(), ItemRequestDto.builder().description("Second request").build());

        List<ItemRequestDto> own = requestService.getOwnRequests(first.getId());
        List<ItemRequestDto> others = requestService.getOtherRequests(first.getId());
        assertEquals(1, own.size());
        assertEquals("First request", own.get(0).getDescription());
        assertEquals(1, others.size());
        assertEquals("Second request", others.get(0).getDescription());
    }
}
