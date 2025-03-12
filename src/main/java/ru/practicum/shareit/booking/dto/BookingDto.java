package ru.practicum.shareit.booking.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.booking.BookingStatus;

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
    @NotNull(message = "itemId не должен быть null")
    private Long itemId;
    @NotNull(message = "bookerId не должен быть null")
    private Long bookerId;
    @NotNull(message = "Статус бронирования не должен быть null")
    private BookingStatus status;
}
