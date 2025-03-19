package ru.practicum.shareit.item.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.booking.dto.BookingShortDto;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ItemOwnerDto extends ItemDto {
    private BookingShortDto lastBooking;
    private BookingShortDto nextBooking;

    public ItemOwnerDto(Long id, String name, String description, Boolean available, Long requestId,
                        BookingShortDto lastBooking, BookingShortDto nextBooking) {
        super(id, name, description, available, requestId);
        this.lastBooking = lastBooking;
        this.nextBooking = nextBooking;
    }
}