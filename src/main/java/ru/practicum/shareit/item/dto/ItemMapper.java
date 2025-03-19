package ru.practicum.shareit.item.dto;

import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.dto.BookingShortDto;

public class ItemMapper {

    public static ItemDto toDto(Item item) {
        return new ItemDto(
                item.getId(),
                item.getName(),
                item.getDescription(),
                item.isAvailable(),
                item.getRequest() != null ? item.getRequest().getId() : null
        );
    }

    public static ItemOwnerDto toOwnerDto(Item item, Booking lastBooking, Booking nextBooking) {
        BookingShortDto last = lastBooking != null
                ? new BookingShortDto(lastBooking.getId(), lastBooking.getStart(), lastBooking.getEnd(), lastBooking.getBooker().getId())
                : null;
        BookingShortDto next = nextBooking != null
                ? new BookingShortDto(nextBooking.getId(), nextBooking.getStart(), nextBooking.getEnd(), nextBooking.getBooker().getId())
                : null;
        return new ItemOwnerDto(
                item.getId(),
                item.getName(),
                item.getDescription(),
                item.isAvailable(),
                item.getRequest() != null ? item.getRequest().getId() : null,
                last,
                next
        );
    }
}