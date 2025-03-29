package ru.practicum.shareit.booking;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingState;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

@SpringBootTest
@Transactional
public class BookingServiceImplTest {

    @Autowired
    private BookingService bookingService;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private UserRepository userRepository;

    private User owner;
    private User booker;
    private User otherUser;
    private Item item;

    @BeforeEach
    public void setUp() {
        owner = new User();
        owner.setName("Owner");
        owner.setEmail("owner@example.com");
        userRepository.save(owner);

        booker = new User();
        booker.setName("Booker");
        booker.setEmail("booker@example.com");
        userRepository.save(booker);

        otherUser = new User();
        otherUser.setName("Other");
        otherUser.setEmail("other@example.com");
        userRepository.save(otherUser);

        item = new Item();
        item.setName("Test Item");
        item.setDescription("This is a test item");
        item.setAvailable(true);
        item.setOwner(owner);
        itemRepository.save(item);
    }

    // --- addBooking() ---

    @Test
    public void testAddBooking_success() {
        BookingDto bookingDto = new BookingDto();
        bookingDto.setStart(LocalDateTime.now().plusDays(1));
        bookingDto.setEnd(LocalDateTime.now().plusDays(2));
        bookingDto.setItemId(item.getId());
        bookingDto.setBookerId(booker.getId());

        BookingDto savedBooking = bookingService.addBooking(bookingDto, booker.getId());

        assertThat(savedBooking).isNotNull();
        assertThat(savedBooking.getId()).isNotNull();
        // Если статус в сервисе устанавливается в WAITING, то он должен быть равен WAITING
        assertThat(savedBooking.getStatus()).isEqualTo(BookingStatus.WAITING);
    }

    @Test
    public void testAddBooking_itemUnavailable() {
        item.setAvailable(false);
        itemRepository.save(item);

        BookingDto bookingDto = new BookingDto();
        bookingDto.setStart(LocalDateTime.now().plusDays(1));
        bookingDto.setEnd(LocalDateTime.now().plusDays(2));
        bookingDto.setItemId(item.getId());
        bookingDto.setBookerId(booker.getId());

        Exception exception = assertThrows(IllegalArgumentException.class, () ->
                bookingService.addBooking(bookingDto, booker.getId()));
        assertThat(exception.getMessage()).isEqualTo("Вещь недоступна для бронирования");
    }

    @Test
    public void testAddBooking_ownerCannotBookOwnItem() {
        BookingDto bookingDto = new BookingDto();
        bookingDto.setStart(LocalDateTime.now().plusDays(1));
        bookingDto.setEnd(LocalDateTime.now().plusDays(2));
        bookingDto.setItemId(item.getId());
        bookingDto.setBookerId(owner.getId());

        Exception exception = assertThrows(ForbiddenException.class, () ->
                bookingService.addBooking(bookingDto, owner.getId()));
        assertThat(exception.getMessage()).isEqualTo("Владелец не может бронировать свою вещь");
    }

    @Test
    public void testAddBooking_invalidDates_equalDates() {
        BookingDto bookingDto = new BookingDto();
        LocalDateTime date = LocalDateTime.now().plusDays(1);
        bookingDto.setStart(date);
        bookingDto.setEnd(date);
        bookingDto.setItemId(item.getId());
        bookingDto.setBookerId(booker.getId());

        Exception exception = assertThrows(IllegalArgumentException.class, () ->
                bookingService.addBooking(bookingDto, booker.getId()));
        assertThat(exception.getMessage()).isEqualTo("Время старта не может быть равно времени окончания");
    }

    @Test
    public void testAddBooking_invalidDates_endBeforeStart() {
        BookingDto bookingDto = new BookingDto();
        bookingDto.setStart(LocalDateTime.now().plusDays(2));
        bookingDto.setEnd(LocalDateTime.now().plusDays(1));
        bookingDto.setItemId(item.getId());
        bookingDto.setBookerId(booker.getId());

        Exception exception = assertThrows(IllegalArgumentException.class, () ->
                bookingService.addBooking(bookingDto, booker.getId()));
        assertThat(exception.getMessage()).isEqualTo("Некорректные даты бронирования");
    }

    @Test
    public void testAddBooking_overlapThrowsException() {
        // Сначала создаем одно бронирование
        BookingDto bookingDto1 = new BookingDto();
        bookingDto1.setStart(LocalDateTime.now().plusDays(1));
        bookingDto1.setEnd(LocalDateTime.now().plusDays(2));
        bookingDto1.setItemId(item.getId());
        bookingDto1.setBookerId(booker.getId());
        bookingService.addBooking(bookingDto1, booker.getId());

        // Пытаемся создать пересекающееся бронирование
        BookingDto bookingDto2 = new BookingDto();
        bookingDto2.setStart(LocalDateTime.now().plusDays(1).plusHours(1));
        bookingDto2.setEnd(LocalDateTime.now().plusDays(2).plusHours(1));
        bookingDto2.setItemId(item.getId());
        bookingDto2.setBookerId(booker.getId());

        Exception exception = assertThrows(IllegalArgumentException.class, () ->
                bookingService.addBooking(bookingDto2, booker.getId()));
        assertThat(exception.getMessage()).isEqualTo("Вещь занята в указанное время");
    }

    // --- approveBooking() ---

    @Test
    public void testApproveBooking_success() {
        BookingDto bookingDto = new BookingDto();
        bookingDto.setStart(LocalDateTime.now().plusDays(1));
        bookingDto.setEnd(LocalDateTime.now().plusDays(2));
        bookingDto.setItemId(item.getId());
        bookingDto.setBookerId(booker.getId());

        BookingDto savedBooking = bookingService.addBooking(bookingDto, booker.getId());
        BookingDto approvedBooking = bookingService.approveBooking(savedBooking.getId(), true, owner.getId());
        assertThat(approvedBooking.getStatus()).isEqualTo(BookingStatus.APPROVED);
    }

    @Test
    public void testApproveBooking_alreadyProcessedThrowsException() {
        BookingDto bookingDto = new BookingDto();
        bookingDto.setStart(LocalDateTime.now().plusDays(1));
        bookingDto.setEnd(LocalDateTime.now().plusDays(2));
        bookingDto.setItemId(item.getId());
        bookingDto.setBookerId(booker.getId());

        BookingDto savedBooking = bookingService.addBooking(bookingDto, booker.getId());
        // Сначала одобряем бронирование
        bookingService.approveBooking(savedBooking.getId(), true, owner.getId());

        Exception exception = assertThrows(IllegalArgumentException.class, () ->
                bookingService.approveBooking(savedBooking.getId(), false, owner.getId()));
        assertThat(exception.getMessage()).isEqualTo("Бронирование уже обработано");
    }

    @Test
    public void testApproveBooking_nonOwnerThrowsException() {
        BookingDto bookingDto = new BookingDto();
        bookingDto.setStart(LocalDateTime.now().plusDays(1));
        bookingDto.setEnd(LocalDateTime.now().plusDays(2));
        bookingDto.setItemId(item.getId());
        bookingDto.setBookerId(booker.getId());

        BookingDto savedBooking = bookingService.addBooking(bookingDto, booker.getId());

        Exception exception = assertThrows(ForbiddenException.class, () ->
                bookingService.approveBooking(savedBooking.getId(), true, booker.getId()));
        assertThat(exception.getMessage()).isEqualTo("Подтверждать бронирование может только владелец вещи");
    }

    // --- getBooking() ---

    @Test
    public void testGetBooking_successByBooker() {
        BookingDto bookingDto = new BookingDto();
        bookingDto.setStart(LocalDateTime.now().plusDays(1));
        bookingDto.setEnd(LocalDateTime.now().plusDays(2));
        bookingDto.setItemId(item.getId());
        bookingDto.setBookerId(booker.getId());

        BookingDto savedBooking = bookingService.addBooking(bookingDto, booker.getId());
        BookingDto fetchedBooking = bookingService.getBooking(savedBooking.getId(), booker.getId());
        assertThat(fetchedBooking).isNotNull();
        assertThat(fetchedBooking.getId()).isEqualTo(savedBooking.getId());
    }

    @Test
    public void testGetBooking_successByOwner() {
        BookingDto bookingDto = new BookingDto();
        bookingDto.setStart(LocalDateTime.now().plusDays(1));
        bookingDto.setEnd(LocalDateTime.now().plusDays(2));
        bookingDto.setItemId(item.getId());
        bookingDto.setBookerId(booker.getId());

        BookingDto savedBooking = bookingService.addBooking(bookingDto, booker.getId());
        BookingDto fetchedBooking = bookingService.getBooking(savedBooking.getId(), owner.getId());
        assertThat(fetchedBooking).isNotNull();
        assertThat(fetchedBooking.getId()).isEqualTo(savedBooking.getId());
    }

    @Test
    public void testGetBooking_forbiddenThrowsException() {
        BookingDto bookingDto = new BookingDto();
        bookingDto.setStart(LocalDateTime.now().plusDays(1));
        bookingDto.setEnd(LocalDateTime.now().plusDays(2));
        bookingDto.setItemId(item.getId());
        bookingDto.setBookerId(booker.getId());

        BookingDto savedBooking = bookingService.addBooking(bookingDto, booker.getId());
        Exception exception = assertThrows(ForbiddenException.class, () ->
                bookingService.getBooking(savedBooking.getId(), otherUser.getId()));
        assertThat(exception.getMessage()).isEqualTo("Доступ запрещён");
    }

    // --- getBookingsByBooker() ---

    @Test
    public void testGetBookingsByBooker_ALL() {
        LocalDateTime now = LocalDateTime.now();

        BookingDto pastBooking = new BookingDto();
        pastBooking.setStart(now.minusDays(3));
        pastBooking.setEnd(now.minusDays(2));
        pastBooking.setItemId(item.getId());
        pastBooking.setBookerId(booker.getId());
        BookingDto savedPast = bookingService.addBooking(pastBooking, booker.getId());

        BookingDto currentBooking = new BookingDto();
        currentBooking.setStart(now.minusHours(1));
        currentBooking.setEnd(now.plusHours(1));
        currentBooking.setItemId(item.getId());
        currentBooking.setBookerId(booker.getId());
        BookingDto savedCurrent = bookingService.addBooking(currentBooking, booker.getId());

        BookingDto futureBooking = new BookingDto();
        futureBooking.setStart(now.plusDays(2));
        futureBooking.setEnd(now.plusDays(3));
        futureBooking.setItemId(item.getId());
        futureBooking.setBookerId(booker.getId());
        BookingDto savedFuture = bookingService.addBooking(futureBooking, booker.getId());

        // Создаем бронирование, которое затем отклоняется
        BookingDto bookingToReject = new BookingDto();
        bookingToReject.setStart(now.plusDays(4));
        bookingToReject.setEnd(now.plusDays(5));
        bookingToReject.setItemId(item.getId());
        bookingToReject.setBookerId(booker.getId());
        BookingDto savedToReject = bookingService.addBooking(bookingToReject, booker.getId());
        bookingService.approveBooking(savedToReject.getId(), false, owner.getId());

        List<BookingDto> allBookings = bookingService.getBookingsByBooker(booker.getId(), BookingState.ALL);
        assertThat(allBookings).hasSize(4);
    }

    @Test
    public void testGetBookingsByBooker_CURRENT() {
        LocalDateTime now = LocalDateTime.now();

        BookingDto currentBooking = new BookingDto();
        currentBooking.setStart(now.minusHours(1));
        currentBooking.setEnd(now.plusHours(1));
        currentBooking.setItemId(item.getId());
        currentBooking.setBookerId(booker.getId());
        BookingDto savedCurrent = bookingService.addBooking(currentBooking, booker.getId());

        List<BookingDto> currentBookings = bookingService.getBookingsByBooker(booker.getId(), BookingState.CURRENT);
        assertThat(currentBookings).extracting(BookingDto::getId).contains(savedCurrent.getId());
    }

    @Test
    public void testGetBookingsByBooker_PAST() {
        LocalDateTime now = LocalDateTime.now();

        BookingDto pastBooking = new BookingDto();
        pastBooking.setStart(now.minusDays(3));
        pastBooking.setEnd(now.minusDays(2));
        pastBooking.setItemId(item.getId());
        pastBooking.setBookerId(booker.getId());
        BookingDto savedPast = bookingService.addBooking(pastBooking, booker.getId());

        List<BookingDto> pastBookings = bookingService.getBookingsByBooker(booker.getId(), BookingState.PAST);
        assertThat(pastBookings).extracting(BookingDto::getId).contains(savedPast.getId());
    }

    @Test
    public void testGetBookingsByBooker_FUTURE() {
        LocalDateTime now = LocalDateTime.now();

        BookingDto futureBooking = new BookingDto();
        futureBooking.setStart(now.plusDays(2));
        futureBooking.setEnd(now.plusDays(3));
        futureBooking.setItemId(item.getId());
        futureBooking.setBookerId(booker.getId());
        BookingDto savedFuture = bookingService.addBooking(futureBooking, booker.getId());

        List<BookingDto> futureBookings = bookingService.getBookingsByBooker(booker.getId(), BookingState.FUTURE);
        assertThat(futureBookings).extracting(BookingDto::getId).contains(savedFuture.getId());
    }

    @Test
    public void testGetBookingsByBooker_WAITING() {
        LocalDateTime now = LocalDateTime.now();

        BookingDto waitingBooking = new BookingDto();
        waitingBooking.setStart(now.plusDays(2));
        waitingBooking.setEnd(now.plusDays(3));
        waitingBooking.setItemId(item.getId());
        waitingBooking.setBookerId(booker.getId());
        BookingDto savedWaiting = bookingService.addBooking(waitingBooking, booker.getId());

        List<BookingDto> waitingBookings = bookingService.getBookingsByBooker(booker.getId(), BookingState.WAITING);
        waitingBookings.forEach(b -> assertThat(b.getStatus()).isEqualTo(BookingStatus.WAITING));
        assertThat(waitingBookings).extracting(BookingDto::getId).contains(savedWaiting.getId());
    }

    @Test
    public void testGetBookingsByBooker_REJECTED() {
        LocalDateTime now = LocalDateTime.now();

        BookingDto bookingToReject = new BookingDto();
        bookingToReject.setStart(now.plusDays(4));
        bookingToReject.setEnd(now.plusDays(5));
        bookingToReject.setItemId(item.getId());
        bookingToReject.setBookerId(booker.getId());
        BookingDto savedToReject = bookingService.addBooking(bookingToReject, booker.getId());
        bookingService.approveBooking(savedToReject.getId(), false, owner.getId());

        List<BookingDto> rejectedBookings = bookingService.getBookingsByBooker(booker.getId(), BookingState.REJECTED);
        rejectedBookings.forEach(b -> assertThat(b.getStatus()).isEqualTo(BookingStatus.REJECTED));
        assertThat(rejectedBookings).extracting(BookingDto::getId).contains(savedToReject.getId());
    }

    // --- getBookingsByOwner() ---

    @Test
    public void testGetBookingsByOwner_ALL() {
        LocalDateTime now = LocalDateTime.now();

        // Создаем одно бронирование
        BookingDto bookingDto = new BookingDto();
        bookingDto.setStart(now.plusDays(2));
        bookingDto.setEnd(now.plusDays(3));
        bookingDto.setItemId(item.getId());
        bookingDto.setBookerId(booker.getId());
        BookingDto savedBooking = bookingService.addBooking(bookingDto, booker.getId());

        List<BookingDto> allBookings = bookingService.getBookingsByOwner(owner.getId(), BookingState.ALL);
        assertThat(allBookings).hasSize(1);
        assertThat(allBookings.get(0).getId()).isEqualTo(savedBooking.getId());
    }

    @Test
    public void testGetBookingsByOwner_CURRENT() {
        LocalDateTime now = LocalDateTime.now();

        BookingDto currentBooking = new BookingDto();
        currentBooking.setStart(now.minusHours(1));
        currentBooking.setEnd(now.plusHours(1));
        currentBooking.setItemId(item.getId());
        currentBooking.setBookerId(booker.getId());
        BookingDto savedCurrent = bookingService.addBooking(currentBooking, booker.getId());

        List<BookingDto> currentBookings = bookingService.getBookingsByOwner(owner.getId(), BookingState.CURRENT);
        assertThat(currentBookings).extracting(BookingDto::getId).contains(savedCurrent.getId());
    }

    @Test
    public void testGetBookingsByOwner_PAST() {
        LocalDateTime now = LocalDateTime.now();

        BookingDto pastBooking = new BookingDto();
        pastBooking.setStart(now.minusHours(3));
        pastBooking.setEnd(now.minusHours(2));
        pastBooking.setItemId(item.getId());
        pastBooking.setBookerId(booker.getId());
        BookingDto savedPast = bookingService.addBooking(pastBooking, booker.getId());

        List<BookingDto> pastBookings = bookingService.getBookingsByOwner(owner.getId(), BookingState.PAST);
        assertThat(pastBookings).extracting(BookingDto::getId).contains(savedPast.getId());
    }

    @Test
    public void testGetBookingsByOwner_FUTURE() {
        LocalDateTime now = LocalDateTime.now();

        BookingDto futureBooking = new BookingDto();
        futureBooking.setStart(now.plusDays(2));
        futureBooking.setEnd(now.plusDays(3));
        futureBooking.setItemId(item.getId());
        futureBooking.setBookerId(booker.getId());
        BookingDto savedFuture = bookingService.addBooking(futureBooking, booker.getId());

        List<BookingDto> futureBookings = bookingService.getBookingsByOwner(owner.getId(), BookingState.FUTURE);
        assertThat(futureBookings).extracting(BookingDto::getId).contains(savedFuture.getId());
    }

    @Test
    public void testGetBookingsByOwner_WAITING() {
        LocalDateTime now = LocalDateTime.now();

        BookingDto waitingBooking = new BookingDto();
        waitingBooking.setStart(now.plusDays(2));
        waitingBooking.setEnd(now.plusDays(3));
        waitingBooking.setItemId(item.getId());
        waitingBooking.setBookerId(booker.getId());
        BookingDto savedWaiting = bookingService.addBooking(waitingBooking, booker.getId());

        List<BookingDto> waitingBookings = bookingService.getBookingsByOwner(owner.getId(), BookingState.WAITING);
        waitingBookings.forEach(b -> assertThat(b.getStatus()).isEqualTo(BookingStatus.WAITING));
        assertThat(waitingBookings).extracting(BookingDto::getId).contains(savedWaiting.getId());
    }

    @Test
    public void testGetBookingsByOwner_REJECTED() {
        LocalDateTime now = LocalDateTime.now();

        BookingDto bookingToReject = new BookingDto();
        bookingToReject.setStart(now.plusDays(4));
        bookingToReject.setEnd(now.plusDays(5));
        bookingToReject.setItemId(item.getId());
        bookingToReject.setBookerId(booker.getId());
        BookingDto savedToReject = bookingService.addBooking(bookingToReject, booker.getId());
        bookingService.approveBooking(savedToReject.getId(), false, owner.getId());

        List<BookingDto> rejectedBookings = bookingService.getBookingsByOwner(owner.getId(), BookingState.REJECTED);
        rejectedBookings.forEach(b -> assertThat(b.getStatus()).isEqualTo(BookingStatus.REJECTED));
        assertThat(rejectedBookings).extracting(BookingDto::getId).contains(savedToReject.getId());
    }
}