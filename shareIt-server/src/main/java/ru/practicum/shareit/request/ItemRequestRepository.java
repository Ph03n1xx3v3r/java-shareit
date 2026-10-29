package ru.practicum.shareit.request;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ItemRequestRepository extends JpaRepository<ItemRequest, Long> {
    List<ItemRequest> findAllByRequestorIdOrderByCreatedDesc(Long requestorId, Pageable pageable);
    List<ItemRequest> findAllByRequestorIdNotOrderByCreatedDesc(Long requestorId, Pageable pageable);

    // Unpaged variants retained for service-level callers and tests.
    List<ItemRequest> findAllByRequestorIdOrderByCreatedDesc(Long requestorId);
    List<ItemRequest> findAllByRequestorIdNotOrderByCreatedDesc(Long requestorId);
}
