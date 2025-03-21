package ru.practicum.shareit.item.dto;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ItemDto {
    private Long id;
    @NotBlank(message = "Название не должно быть пустым")
    private String name;
    @NotBlank(message = "Описание не должно быть пустым")
    private String description;
    @NotNull(message = "Поле available должно быть указано")
    private Boolean available;
    private Long requestId;
    private List<CommentDto> comments;
}