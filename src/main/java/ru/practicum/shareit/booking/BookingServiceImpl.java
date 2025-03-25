package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingMapper;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;

    @Override
    public BookingDto addBooking(BookingDto bookingDto, Long bookerId) {
        User booker = getUserById(bookerId);
        Item item = getItemById(bookingDto.getItemId());
        if (!item.isAvailable()) {
            throw new IllegalArgumentException("Вещь недоступна для бронирования");
        }
        if (item.getOwner().getId().equals(bookerId)) {
            throw new ForbiddenException("Владелец не может бронировать свою вещь");
        }
        validateBookingDates(bookingDto);
        validateBookingOverlap(bookingDto, item);
        bookingDto.setStatus(BookingStatus.WAITING);
        Booking booking = BookingMapper.toEntity(bookingDto, item, booker);
        bookingRepository.save(booking);
        return BookingMapper.toDto(booking);
    }

    @Override
    public BookingDto approveBooking(Long bookingId, Boolean approved, Long ownerId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Бронирование не найдено"));
        if (!booking.getItem().getOwner().getId().equals(ownerId)) {
            throw new ForbiddenException("Подтверждать бронирование может только владелец вещи");
        }
        if (!booking.getStatus().equals(BookingStatus.WAITING)) {
            throw new IllegalArgumentException("Бронирование уже обработано");
        }
        booking.setStatus(approved ? BookingStatus.APPROVED : BookingStatus.REJECTED);
        bookingRepository.save(booking);
        return BookingMapper.toDto(booking);
    }

    @Override
    public BookingDto getBooking(Long bookingId, Long userId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Бронирование не найдено"));
        if (!booking.getBooker().getId().equals(userId) &&
            !booking.getItem().getOwner().getId().equals(userId)) {
            throw new ForbiddenException("Доступ запрещён");
        }
        return BookingMapper.toDto(booking);
    }

    @Override
    public List<BookingDto> getBookingsByBooker(Long bookerId, BookingState state) {
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
        return bookings.stream()
                .map(BookingMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<BookingDto> getBookingsByOwner(Long ownerId, BookingState state) {
        List<Item> items = itemRepository.findByOwnerId(ownerId);
        if (items.isEmpty()) {
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
        return bookings.stream()
                .map(BookingMapper::toDto)
                .collect(Collectors.toList());
    }

    // Приватные методы для уменьшения дублирования кода

    private User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));
    }

    private Item getItemById(Long id) {
        return itemRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Вещь не найдена"));
    }

    private void validateBookingDates(BookingDto bookingDto) {
        LocalDateTime start = bookingDto.getStart();
        LocalDateTime end = bookingDto.getEnd();
        if (start.equals(end)) {
            throw new IllegalArgumentException("Время старта не может быть равно времени окончания");
        }
        if (start.isBefore(LocalDateTime.now()) || end.isBefore(start)) {
            throw new IllegalArgumentException("Некорректные даты бронирования");
        }
    }

    private void validateBookingOverlap(BookingDto bookingDto, Item item) {
        List<Booking> overlapping = bookingRepository
                .findByItemIdAndStatusIn(item.getId(), List.of(BookingStatus.APPROVED, BookingStatus.WAITING))
                .stream()
                .filter(b -> bookingDto.getStart().isBefore(b.getEndTime()) && bookingDto.getEnd().isAfter(b.getStartTime()))
                .toList();
        if (!overlapping.isEmpty()) {
            throw new IllegalArgumentException("Вещь занята в указанное время");
        }
    }
}