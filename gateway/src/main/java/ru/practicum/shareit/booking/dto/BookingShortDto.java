package ru.practicum.shareit.booking.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookingShortDto {

    private Long id;

    @NotNull(message = "Start time не должно быть null")
    private LocalDateTime start;

    @NotNull(message = "End time не должно быть null")
    private LocalDateTime end;

    @NotNull(message = "bookerId не должно быть null")
    private Long bookerId;
}