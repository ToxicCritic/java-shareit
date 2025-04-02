package ru.practicum.shareit.item;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import ru.practicum.shareit.item.comment.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.request.ItemRequestRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.BookingService;

@SpringBootTest
@Transactional
public class ItemServiceImplTest {

    @Autowired
    private ItemService itemService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ItemRequestRepository itemRequestRepository;

    @Autowired
    private BookingService bookingService;

    private User owner;
    private User otherUser;
    private ItemRequest request;
    private ItemDto createdItem;

    @BeforeEach
    void setUp() {
        owner = new User();
        owner.setName("Owner");
        owner.setEmail("owner@example.com");
        owner = userRepository.save(owner);

        otherUser = new User();
        otherUser.setName("Other");
        otherUser.setEmail("other@example.com");
        otherUser = userRepository.save(otherUser);

        request = new ItemRequest();
        request.setDescription("Нужна дрель");
        request.setRequestor(otherUser);
        request.setCreated(LocalDateTime.now().minusDays(1));
        request = itemRequestRepository.save(request);

        ItemDto itemDto = new ItemDto();
        itemDto.setName("Test Item");
        itemDto.setDescription("Description");
        itemDto.setAvailable(true);
        itemDto.setRequestId(null);
        itemDto.setComments(List.of());

        createdItem = itemService.addItem(itemDto, owner.getId());
    }

    @Test
    void testAddItem_success() {
        ItemDto created = itemService.addItem(createdItem, owner.getId());
        assertThat(created.getId()).isNotNull();
        assertThat(created.getName()).isEqualTo("Test Item");
        assertThat(created.getRequestId()).isNull();
    }

    @Test
    void testAddItem_withRequestId_success() {
        createdItem.setRequestId(request.getId());
        ItemDto created = itemService.addItem(createdItem, owner.getId());
        assertThat(created.getId()).isNotNull();
        assertThat(created.getRequestId()).isEqualTo(request.getId());
    }

    @Test
    void testUpdateItem_success() {
        ItemDto created = itemService.addItem(createdItem, owner.getId());
        created.setName("Updated Hammer");
        created.setDescription("Updated Description");
        created.setAvailable(false);
        ItemDto updated = itemService.updateItem(created.getId(), created, owner.getId());
        assertThat(updated.getName()).isEqualTo("Updated Hammer");
        assertThat(updated.getDescription()).isEqualTo("Updated Description");
        assertThat(updated.getAvailable()).isFalse();
    }

    @Test
    void testUpdateItem_notOwnerThrowsException() {
        ItemDto created = itemService.addItem(createdItem, owner.getId());
        Exception ex = assertThrows(ForbiddenException.class, () ->
                itemService.updateItem(created.getId(), created, otherUser.getId()));
        assertThat(ex.getMessage()).isEqualTo("Изменять вещь может только её владелец");
    }

    @Test
    void testGetItemById_owner() {
        ItemDto created = itemService.addItem(createdItem, owner.getId());
        ItemDto fetched = itemService.getItemById(created.getId(), owner.getId());
        assertThat(fetched.getId()).isEqualTo(created.getId());
    }

    @Test
    void testGetItemById_nonOwner() {
        ItemDto created = itemService.addItem(createdItem, owner.getId());
        ItemDto fetched = itemService.getItemById(created.getId(), otherUser.getId());
        assertThat(fetched.getId()).isEqualTo(created.getId());
    }

    @Test
    void testGetItemsByOwner_success() {
        ItemDto another = new ItemDto();
        another.setName("Screwdriver");
        another.setDescription("Sharp screwdriver");
        another.setAvailable(true);
        another.setRequestId(null);
        another.setComments(List.of());
        itemService.addItem(another, owner.getId());

        List<ItemDto> items = itemService.getItemsByOwner(owner.getId());
        assertThat(items).hasSizeGreaterThanOrEqualTo(2);
    }

    @Test
    void testSearchItems_returnsMatchingItems() {
        // Добавляем доступную и недоступную вещь
        ItemDto availableDto = new ItemDto();
        availableDto.setName("Searchable Item");
        availableDto.setDescription("This item is searchable");
        availableDto.setAvailable(true);
        availableDto.setRequestId(null);
        availableDto.setComments(List.of());
        itemService.addItem(availableDto, owner.getId());

        ItemDto unavailableDto = new ItemDto();
        unavailableDto.setName("Not Searchable Item");
        unavailableDto.setDescription("This item is not available");
        unavailableDto.setAvailable(false);
        unavailableDto.setRequestId(null);
        unavailableDto.setComments(List.of());
        itemService.addItem(unavailableDto, owner.getId());

        List<ItemDto> results = itemService.searchItems("searchable");
        assertThat(results).extracting(ItemDto::getName).contains("Searchable Item");
        assertThat(results).extracting(ItemDto::getName).doesNotContain("Not Searchable Item");
    }

    @Test
    void testAddComment_withoutPastBookingThrowsException() {
        // Добавляем вещь
        ItemDto created = itemService.addItem(createdItem, owner.getId());
        CommentDto comment = new CommentDto();
        comment.setText("Nice item!");
        Exception ex = assertThrows(IllegalArgumentException.class, () ->
                itemService.addComment(created.getId(), otherUser.getId(), comment));
        assertThat(ex.getMessage()).contains("Пользователь не брал вещь в аренду");
    }

    @Test
    void testAddComment_success() {
        BookingDto bookingDto = new BookingDto();
        bookingDto.setStart(LocalDateTime.now().minusDays(3));
        bookingDto.setEnd(LocalDateTime.now().minusDays(2));
        bookingDto.setItemId(createdItem.getId());
        bookingDto.setBookerId(otherUser.getId());
        BookingDto pastBooking = bookingService.addBooking(bookingDto, otherUser.getId());
        bookingService.approveBooking(pastBooking.getId(), true, owner.getId());

        CommentDto comment = new CommentDto();
        comment.setText("Great item!");
        CommentDto savedComment = itemService.addComment(createdItem.getId(), otherUser.getId(), comment);
        assertThat(savedComment).isNotNull();
        assertThat(savedComment.getId()).isNotNull();
        assertThat(savedComment.getText()).isEqualTo("Great item!");
        assertThat(savedComment.getAuthorName()).isEqualTo(otherUser.getName());
    }
}