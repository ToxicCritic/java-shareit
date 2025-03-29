package ru.practicum.shareit.item;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.item.comment.dto.CommentDto;
import ru.practicum.shareit.booking.BookingRepository;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Transactional
class ItemServiceImplTest {

    @Autowired
    private ItemService itemService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BookingRepository bookingRepository;

    private User owner;
    private User otherUser;

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
    }

    @Test
    void testAddItemAndGetItemById() {
        ItemDto itemDto = new ItemDto();
        itemDto.setName("Integration Item");
        itemDto.setDescription("Integration Description");
        itemDto.setAvailable(true);
        itemDto.setRequestId(null);
        itemDto.setComments(List.of());

        ItemDto created = itemService.addItem(itemDto, owner.getId());
        assertThat(created.getId()).isNotNull();
        assertThat(created.getName()).isEqualTo("Integration Item");

        ItemDto fetched = itemService.getItemById(created.getId(), owner.getId());
        assertThat(fetched.getId()).isEqualTo(created.getId());
        assertThat(fetched.getName()).isEqualTo("Integration Item");
    }

    @Test
    void testUpdateItem() {
        ItemDto itemDto = new ItemDto();
        itemDto.setName("Original Item");
        itemDto.setDescription("Original Description");
        itemDto.setAvailable(true);
        itemDto.setRequestId(null);
        itemDto.setComments(List.of());

        ItemDto created = itemService.addItem(itemDto, owner.getId());

        created.setName("Updated Item");
        created.setDescription("Updated Description");
        created.setAvailable(false);

        ItemDto updated = itemService.updateItem(created.getId(), created, owner.getId());
        assertThat(updated.getName()).isEqualTo("Updated Item");
        assertThat(updated.getDescription()).isEqualTo("Updated Description");
        assertThat(updated.getAvailable()).isFalse();
    }

    @Test
    void testAddCommentWithoutPastBooking() {
        ItemDto itemDto = new ItemDto();
        itemDto.setName("Commentable Item");
        itemDto.setDescription("Description");
        itemDto.setAvailable(true);
        itemDto.setRequestId(null);
        itemDto.setComments(List.of());
        ItemDto created = itemService.addItem(itemDto, owner.getId());

        CommentDto comment = new CommentDto();
        comment.setText("Nice item");

        Exception exception = assertThrows(IllegalArgumentException.class, () ->
                itemService.addComment(created.getId(), otherUser.getId(), comment));
        assertThat(exception.getMessage()).contains("Пользователь не брал вещь в аренду");
    }
}