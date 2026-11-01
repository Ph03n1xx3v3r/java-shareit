package ru.practicum.shareit.request.reposirtory;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.shareit.request.model.Request;

import java.util.List;

public interface ItemRequestRepository extends JpaRepository<Request, Long> {

    @Query(" select r from Request r where r.user.id <> :userId ")
    List<Request> getAllRequestWithoutUsersRequest(@Param("userId") Long userId);

    @Query(" select r from Request r where r.user.id = :userId " +
            " order by r.created desc ")
    List<Request> findAllRequestByUserId(@Param("userId") Long userId);

}
