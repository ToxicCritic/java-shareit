package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingMapper;
import ru.practicum.shareit.booking.dto.BookingState;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;

    @Override
    public BookingDto addBooking(BookingDto bookingDto, Long bookerId) {
        log.info("Called addBooking(bookingDto={}, bookerId={})", bookingDto, bookerId);

        User booker = getUserById(bookerId);
        Item item = getItemById(bookingDto.getItemId());
        if (!item.isAvailable()) {
            log.warn("Item {} is not available for booking. Throwing exception.", item.getId());
            throw new IllegalArgumentException("Вещь недоступна для бронирования");
        }
        if (item.getOwner().getId().equals(bookerId)) {
            log.warn("User {} is owner of item {} and cannot book it. Throwing exception.", bookerId, item.getId());
            throw new ForbiddenException("Владелец не может бронировать свою вещь");
        }
        validateBookingDates(bookingDto);
        validateBookingOverlap(bookingDto, item);
        if (bookingDto.getStatus() == null) {
            bookingDto.setStatus(BookingStatus.WAITING);
        }

        Booking booking = BookingMapper.toEntity(bookingDto, item, booker);
        bookingRepository.save(booking);

        log.debug("Booking saved: {}", booking);
        return BookingMapper.toDto(booking);
    }

    @Override
    public BookingDto approveBooking(Long bookingId, Boolean approved, Long ownerId) {
        log.info("Called approveBooking(bookingId={}, approved={}, ownerId={})",
                bookingId, approved, ownerId);

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> {
                    log.warn("Booking with id={} not found", bookingId);
                    return new NotFoundException("Бронирование не найдено");
                });
        if (!booking.getItem().getOwner().getId().equals(ownerId)) {
            log.warn("User {} is not owner of item {}. Throwing ForbiddenException.",
                    ownerId, booking.getItem().getId());
            throw new ForbiddenException("Подтверждать бронирование может только владелец вещи");
        }
        if (!booking.getStatus().equals(BookingStatus.WAITING)) {
            log.warn("Booking {} is not in WAITING status. Current status={}. Cannot approve/reject again.",
                    bookingId, booking.getStatus());
            throw new IllegalArgumentException("Бронирование уже обработано");
        }
        booking.setStatus(approved ? BookingStatus.APPROVED : BookingStatus.REJECTED);
        bookingRepository.save(booking);

        log.debug("Booking {} approved={} by owner {}. New status={}",
                bookingId, approved, ownerId, booking.getStatus());
        return BookingMapper.toDto(booking);
    }

    @Override
    public BookingDto getBooking(Long bookingId, Long userId) {
        log.info("Called getBooking(bookingId={}, userId={})", bookingId, userId);

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> {
                    log.warn("Booking with id={} not found", bookingId);
                    return new NotFoundException("Бронирование не найдено");
                });
        if (!booking.getBooker().getId().equals(userId) &&
            !booking.getItem().getOwner().getId().equals(userId)) {
            log.warn("User {} has no rights to see booking {}", userId, bookingId);
            throw new ForbiddenException("Доступ запрещён");
        }
        log.debug("Booking found: {}", booking);
        return BookingMapper.toDto(booking);
    }

    @Override
    public List<BookingDto> getBookingsByBooker(Long bookerId, BookingState state) {
        log.info("Called getBookingsByBooker(bookerId={}, state={})", bookerId, state);
        LocalDateTime now = LocalDateTime.now();

        List<Booking> bookings = switch (state == null ? BookingState.ALL : state) {
            case CURRENT ->
                    bookingRepository.findByBookerIdAndStartTimeBeforeAndEndTimeAfterOrderByStartTimeDesc(bookerId, now, now);
            case PAST ->
                    bookingRepository.findByBookerIdAndEndTimeBeforeOrderByStartTimeDesc(bookerId, now);
            case FUTURE ->
                    bookingRepository.findByBookerIdAndStartTimeAfterOrderByStartTimeDesc(bookerId, now);
            case WAITING ->
                    bookingRepository.findByBookerIdAndStatusOrderByStartTimeDesc(bookerId, BookingStatus.WAITING);
            case REJECTED ->
                    bookingRepository.findByBookerIdAndStatusOrderByStartTimeDesc(bookerId, BookingStatus.REJECTED);
            default ->
                    bookingRepository.findByBookerIdOrderByStartTimeDesc(bookerId);
        };

        log.debug("Found {} bookings for bookerId={} and state={}", bookings.size(), bookerId, state);
        return bookings.stream()
                .map(BookingMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<BookingDto> getBookingsByOwner(Long ownerId, BookingState state) {
        log.info("Called getBookingsByOwner(ownerId={}, state={})", ownerId, state);

        List<Item> items = itemRepository.findByOwnerId(ownerId);
        if (items.isEmpty()) {
            log.warn("Owner {} has no items, so no bookings can be found", ownerId);
            throw new NotFoundException("Пользователь не имеет вещей, бронирования не найдены");
        }
        LocalDateTime now = LocalDateTime.now();
        List<Booking> bookings = switch (state == null ? BookingState.ALL : state) {
            case CURRENT ->
                    bookingRepository.findByItemOwnerIdAndStartTimeBeforeAndEndTimeAfterOrderByStartTimeDesc(ownerId, now, now);
            case PAST ->
                    bookingRepository.findByItemOwnerIdAndEndTimeBeforeOrderByStartTimeDesc(ownerId, now);
            case FUTURE ->
                    bookingRepository.findByItemOwnerIdAndStartTimeAfterOrderByStartTimeDesc(ownerId, now);
            case WAITING ->
                    bookingRepository.findByItemOwnerIdAndStatusOrderByStartTimeDesc(ownerId, BookingStatus.WAITING);
            case REJECTED ->
                    bookingRepository.findByItemOwnerIdAndStatusOrderByStartTimeDesc(ownerId, BookingStatus.REJECTED);
            default ->
                    bookingRepository.findByItemOwnerIdOrderByStartTimeDesc(ownerId);
        };

        log.debug("Found {} bookings for ownerId={} and state={}", bookings.size(), ownerId, state);
        return bookings.stream()
                .map(BookingMapper::toDto)
                .collect(Collectors.toList());
    }

    private User getUserById(Long id) {
        log.debug("Fetching user by id={}", id);
        return userRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("User with id={} not found", id);
                    return new NotFoundException("Пользователь не найден");
                });
    }

    private Item getItemById(Long id) {
        log.debug("Fetching item by id={}", id);
        return itemRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Item with id={} not found", id);
                    return new NotFoundException("Вещь не найдена");
                });
    }

    private void validateBookingDates(BookingDto bookingDto) {
        log.debug("Validating booking dates: start={}, end={}", bookingDto.getStart(), bookingDto.getEnd());
        LocalDateTime start = bookingDto.getStart();
        LocalDateTime end = bookingDto.getEnd();
        if (start.equals(end)) {
            log.warn("Booking start={} equals end={}. Throwing exception.", start, end);
            throw new IllegalArgumentException("Время старта не может быть равно времени окончания");
        }
        if (end.isBefore(start)) {
            log.warn("Dates are incorrect: start={}, end={}. Throwing exception.", start, end);
            throw new IllegalArgumentException("Некорректные даты бронирования");
        }
    }

    private void validateBookingOverlap(BookingDto bookingDto, Item item) {
        log.debug("Checking overlap for itemId={} with bookingDto={}", item.getId(), bookingDto);
        List<Booking> overlapping = bookingRepository
                .findByItemIdAndStatusIn(item.getId(), List.of(BookingStatus.APPROVED, BookingStatus.WAITING))
                .stream()
                .filter(b -> bookingDto.getStart().isBefore(b.getEndTime())
                             && bookingDto.getEnd().isAfter(b.getStartTime()))
                .toList();
        if (!overlapping.isEmpty()) {
            log.warn("Found overlap. bookingDto={} overlaps with bookings: {}", bookingDto, overlapping);
            throw new IllegalArgumentException("Вещь занята в указанное время");
        }
    }
}