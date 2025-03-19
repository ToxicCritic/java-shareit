package ru.practicum.shareit.booking;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByBookerIdOrderByStartDesc(Long bookerId);
    List<Booking> findByItemOwnerIdOrderByStartDesc(Long ownerId);
    Optional<Booking> findFirstByItemIdAndStartBeforeOrderByStartDesc(Long itemId, LocalDateTime now);
    Optional<Booking> findFirstByItemIdAndStartAfterOrderByStartAsc(Long itemId, LocalDateTime now);
}