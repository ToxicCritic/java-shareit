package ru.practicum.shareit.booking.dto;

import lombok.extern.slf4j.Slf4j;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemMapper;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserMapper;

@Slf4j
public class BookingMapper {

    public static Booking toEntity(BookingDto dto, Item item, User booker) {
        if (dto == null) {
            return null;
        }
        Booking booking = new Booking();
        booking.setId(dto.getId());
        booking.setStartTime(dto.getStart());
        booking.setEndTime(dto.getEnd());
        booking.setStatus(dto.getStatus());
        booking.setItem(item);
        booking.setBooker(booker);
        return booking;
    }

    public static BookingDto toDto(Booking booking) {
        if (booking == null) {
            return null;
        }
        BookingDto dto = new BookingDto();
        dto.setId(booking.getId());
        dto.setStart(booking.getStartTime());
        dto.setEnd(booking.getEndTime());
        User booker = booking.getBooker();
        Item item = booking.getItem();
        dto.setStatus(booking.getStatus());

        if (booker != null) {
            dto.setBookerId(booker.getId());
            UserDto bookerDto = UserMapper.toDto(booker);
            dto.setBooker(bookerDto);
        }
        if (item != null) {
            dto.setItemId(item.getId());
            ItemDto itemDto = ItemMapper.toDto(item);
            dto.setItem(itemDto);
        }
        return dto;
    }
}