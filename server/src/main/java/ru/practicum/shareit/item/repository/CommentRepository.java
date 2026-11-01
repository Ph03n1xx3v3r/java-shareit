package ru.practicum.shareit.item.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.shareit.item.model.Comment;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    @Query("select c from Comment c " +
            "where c.item.id = :itemId " +
            " order by c.created desc")
    List<Comment> getCommetListForItem(@Param("itemId") Long itemId);

    @Query("select c from Comment c " +
            "where c.item.id in :itemIds " +
            " order by c.created desc")
    List<Comment> getCommetListForItemIds(@Param("itemIds") List<Long> itemIds);

}
