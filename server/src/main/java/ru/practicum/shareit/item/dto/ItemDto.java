package ru.practicum.shareit.item.dto;

import lombok.*;
import ru.practicum.shareit.item.comment.dto.CommentDto;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ItemDto {
    private Long id;
    private String name;
    private String description;
    private Boolean available;
    private Long requestId;
    private List<CommentDto> comments;
}