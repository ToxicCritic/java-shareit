package ru.practicum.shareit.request.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.item.dto.ItemShortDto;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ItemRequestDto {
    private Long id;
    @NotBlank(message = "Описание запроса не должно быть пустым")
    private String description;
    @NotNull(message = "requestorId не должен быть null")
    private Long requestorId;
    private LocalDateTime created;
    private List<ItemShortDto> items;
}
