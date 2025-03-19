package ru.practicum.shareit.booking;

import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingDto;
import java.util.List;

@RestController
@RequestMapping("/bookings")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping
    public BookingDto addBooking(@RequestBody BookingDto bookingDto,
                                 @RequestHeader("X-Sharer-User-Id") Long bookerId) {
        return bookingService.addBooking(bookingDto, bookerId);
    }

    @PatchMapping("/{bookingId}")
    public BookingDto approveBooking(@PathVariable Long bookingId,
                                     @RequestParam Boolean approved,
                                     @RequestHeader("X-Sharer-User-Id") Long ownerId) {
        return bookingService.approveBooking(bookingId, approved, ownerId);
    }

    @GetMapping("/{bookingId}")
    public BookingDto getBooking(@PathVariable Long bookingId,
                                 @RequestHeader("X-Sharer-User-Id") Long userId) {
        return bookingService.getBooking(bookingId, userId);
    }

    @GetMapping
    public List<BookingDto> getBookingsByBooker(@RequestHeader("X-Sharer-User-Id") Long bookerId,
                                                @RequestParam(required = false) String state) {
        return bookingService.getBookingsByBooker(bookerId, state);
    }

    @GetMapping("/owner")
    public List<BookingDto> getBookingsByOwner(@RequestHeader("X-Sharer-User-Id") Long ownerId,
                                               @RequestParam(required = false) String state) {
        return bookingService.getBookingsByOwner(ownerId, state);
    }
}