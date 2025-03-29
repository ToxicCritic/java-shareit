package ru.practicum.shareit.item.dto;

import ru.practicum.shareit.item.comment.dto.CommentDto;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.dto.BookingShortDto;
import java.util.List;

public class ItemMapper {

    public static ItemDto toDto(Item item, List<CommentDto> comments) {
        return new ItemDto(
                item.getId(),
                item.getName(),
                item.getDescription(),
                item.isAvailable(),
                item.getRequest() != null ? item.getRequest().getId() : null,
                comments
        );
    }

    public static ItemDto toDto(Item item) {
        return toDto(item, null);
    }

    public static ItemOwnerDto toOwnerDto(Item item, Booking lastBooking, Booking nextBooking, List<CommentDto> comments) {
        BookingShortDto last = lastBooking != null
                ? new BookingShortDto(lastBooking.getId(), lastBooking.getStartTime(), lastBooking.getEndTime(), lastBooking.getBooker().getId())
                : null;
        BookingShortDto next = nextBooking != null
                ? new BookingShortDto(nextBooking.getId(), nextBooking.getStartTime(), nextBooking.getEndTime(), nextBooking.getBooker().getId())
                : null;
        return new ItemOwnerDto(
                item.getId(),
                item.getName(),
                item.getDescription(),
                item.isAvailable(),
                item.getRequest() != null ? item.getRequest().getId() : null,
                comments,
                last,
                next
        );
    }
}