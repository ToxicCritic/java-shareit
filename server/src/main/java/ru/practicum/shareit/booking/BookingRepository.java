package ru.practicum.shareit.booking;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByBookerIdOrderByStartTimeDesc(Long bookerId);

    Optional<Booking> findFirstByItemIdAndStartTimeBeforeOrderByStartTimeDesc(Long itemId, LocalDateTime now);

    Optional<Booking> findFirstByItemIdAndStartTimeAfterOrderByStartTimeAsc(Long itemId, LocalDateTime now);

    List<Booking> findByItemIdAndStatusIn(Long itemId, List<BookingStatus> statuses);

    List<Booking> findByBookerIdAndStartTimeBeforeAndEndTimeAfterOrderByStartTimeDesc(Long bookerId, LocalDateTime now1, LocalDateTime now2);

    List<Booking> findByBookerIdAndEndTimeBeforeOrderByStartTimeDesc(Long bookerId, LocalDateTime now);

    List<Booking> findByBookerIdAndStartTimeAfterOrderByStartTimeDesc(Long bookerId, LocalDateTime now);

    List<Booking> findByBookerIdAndStatusOrderByStartTimeDesc(Long bookerId, BookingStatus status);

    List<Booking> findByBookerIdAndItemIdAndStatus(Long bookerId, Long itemId, BookingStatus status);

    List<Booking> findByItemOwnerIdOrderByStartTimeDesc(Long ownerId);

    List<Booking> findByItemOwnerIdAndStartTimeBeforeAndEndTimeAfterOrderByStartTimeDesc(Long ownerId, LocalDateTime now1, LocalDateTime now2);

    List<Booking> findByItemOwnerIdAndEndTimeBeforeOrderByStartTimeDesc(Long ownerId, LocalDateTime now);

    List<Booking> findByItemOwnerIdAndStartTimeAfterOrderByStartTimeDesc(Long ownerId, LocalDateTime now);

    List<Booking> findByItemOwnerIdAndStatusOrderByStartTimeDesc(Long ownerId, BookingStatus status);
}