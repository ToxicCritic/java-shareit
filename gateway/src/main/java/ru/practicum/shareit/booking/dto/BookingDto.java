package ru.practicum.shareit.booking.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.dto.UserDto;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookingDto {
    private Long id;

    @NotNull(message = "Начало бронирования не должно быть null")
    private LocalDateTime start;

    @NotNull(message = "Конец бронирования не должен быть null")
    private LocalDateTime end;

    private Long itemId;
    private Long bookerId;

    private ItemDto item;
    private UserDto booker;

    private BookingStatus status;
}