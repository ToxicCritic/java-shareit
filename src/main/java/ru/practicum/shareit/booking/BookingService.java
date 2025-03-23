package ru.practicum.shareit.booking;

import ru.practicum.shareit.booking.dto.BookingDto;
import java.util.List;

public interface BookingService {
    BookingDto addBooking(BookingDto bookingDto, Long bookerId);

    BookingDto approveBooking(Long bookingId, Boolean approved, Long ownerId);

    BookingDto getBooking(Long bookingId, Long userId);

    List<BookingDto> getBookingsByBooker(Long bookerId, BookingState state);

    List<BookingDto> getBookingsByOwner(Long ownerId, BookingState state);
}