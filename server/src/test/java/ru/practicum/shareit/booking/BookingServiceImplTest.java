package ru.practicum.shareit.booking;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import ru.practicum.shareit.booking.dto.BookingDto;
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

        item = new Item();
        item.setName("Test Item");
        item.setDescription("This is a test item");
        item.setAvailable(true);
        item.setOwner(owner);
        itemRepository.save(item);
    }

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

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            bookingService.addBooking(bookingDto, booker.getId());
        });

        assertThat(exception.getMessage()).isEqualTo("Вещь недоступна для бронирования");
    }

    @Test
    public void testAddBooking_ownerCannotBookOwnItem() {
        BookingDto bookingDto = new BookingDto();
        bookingDto.setStart(LocalDateTime.now().plusDays(1));
        bookingDto.setEnd(LocalDateTime.now().plusDays(2));
        bookingDto.setItemId(item.getId());
        bookingDto.setBookerId(owner.getId());

        Exception exception = assertThrows(ForbiddenException.class, () -> {
            bookingService.addBooking(bookingDto, owner.getId());
        });

        assertThat(exception.getMessage()).isEqualTo("Владелец не может бронировать свою вещь");
    }

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
    public void testGetBooking_success() {
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

}