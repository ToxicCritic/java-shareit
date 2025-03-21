package ru.practicum.shareit.item.dto;

import ru.practicum.shareit.item.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import java.time.LocalDateTime;

public class CommentMapper {

    public static CommentDto toDto(Comment comment) {
        if (comment == null) return null;
        return new CommentDto(
                comment.getId(),
                comment.getText(),
                comment.getAuthor().getName(),
                comment.getCreated()
        );
    }

    public static Comment toEntity(CommentDto dto, Item item, User author) {
        if (dto == null) return null;
        Comment comment = new Comment();
        comment.setText(dto.getText());
        comment.setItem(item);
        comment.setAuthor(author);
        comment.setCreated(LocalDateTime.now());
        return comment;
    }
}