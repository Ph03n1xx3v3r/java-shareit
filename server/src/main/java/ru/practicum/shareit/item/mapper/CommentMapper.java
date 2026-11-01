package ru.practicum.shareit.item.mapper;

import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;

@Component
@NoArgsConstructor
public class CommentMapper {
    public static Comment toComment(CommentDto commentDto, User user, Item item) {
        Comment comment = new Comment();
        comment.setUser(user);
        comment.setItem(item);
        comment.setText(commentDto.getText());
        comment.setCreated(LocalDateTime.now());
        return comment;
    }

    public static CommentDto toCommentDto(Comment comment, User user) {
        CommentDto commentDto = new CommentDto();
        commentDto.setCreated(comment.getCreated());
        commentDto.setText(comment.getText());
        commentDto.setId(comment.getId());
        commentDto.setAuthorName(user.getName());
        return commentDto;
    }
}
