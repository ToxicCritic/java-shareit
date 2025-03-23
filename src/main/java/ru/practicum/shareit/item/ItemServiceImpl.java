package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.comment.CommentRepository;
import ru.practicum.shareit.item.comment.dto.CommentDto;
import ru.practicum.shareit.item.comment.dto.CommentMapper;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;

    @Override
    public ItemDto addItem(ItemDto itemDto, Long ownerId) {
        User owner = getUserById(ownerId);
        Item item = new Item();
        item.setName(itemDto.getName());
        item.setDescription(itemDto.getDescription());
        item.setAvailable(itemDto.getAvailable());
        item.setOwner(owner);
        itemRepository.save(item);
        return ItemMapper.toDto(item, List.of());
    }

    @Override
    public ItemDto updateItem(Long itemId, ItemDto itemDto, Long ownerId) {
        Item item = getItemById(itemId);
        if (!item.getOwner().getId().equals(ownerId)) {
            throw new ForbiddenException("Изменять вещь может только её владелец");
        }
        if (itemDto.getName() != null) {
            item.setName(itemDto.getName());
        }
        if (itemDto.getDescription() != null) {
            item.setDescription(itemDto.getDescription());
        }
        if (itemDto.getAvailable() != null) {
            item.setAvailable(itemDto.getAvailable());
        }
        itemRepository.save(item);
        List<CommentDto> comments = commentRepository.findByItemIdOrderByCreatedAsc(item.getId())
                .stream().map(CommentMapper::toDto).collect(Collectors.toList());
        return ItemMapper.toDto(item, comments);
    }

    @Override
    public ItemDto getItemById(Long itemId, Long requesterId) {
        Item item = getItemById(itemId);
        List<CommentDto> comments = commentRepository.findByItemIdOrderByCreatedAsc(item.getId())
                .stream().map(CommentMapper::toDto).collect(Collectors.toList());
        if (item.getOwner().getId().equals(requesterId)) {
            Booking lastBooking = bookingRepository
                    .findFirstByItemIdAndStartTimeBeforeOrderByStartTimeDesc(itemId, LocalDateTime.now())
                    .orElse(null);
            Booking nextBooking = bookingRepository
                    .findFirstByItemIdAndStartTimeAfterOrderByStartTimeAsc(itemId, LocalDateTime.now())
                    .orElse(null);
            return ItemMapper.toOwnerDto(item, lastBooking, nextBooking, comments);
        }
        return ItemMapper.toOwnerDto(item, null, null, comments);
    }

    @Override
    public List<ItemDto> getItemsByOwner(Long ownerId) {
        List<Item> items = itemRepository.findByOwnerId(ownerId);
        return items.stream()
                .map(item -> {
                    List<CommentDto> comments = commentRepository.findByItemIdOrderByCreatedAsc(item.getId())
                            .stream().map(CommentMapper::toDto).collect(Collectors.toList());
                    Booking lastBooking = bookingRepository
                            .findFirstByItemIdAndStartTimeBeforeOrderByStartTimeDesc(item.getId(), LocalDateTime.now())
                            .orElse(null);
                    Booking nextBooking = bookingRepository
                            .findFirstByItemIdAndStartTimeAfterOrderByStartTimeAsc(item.getId(), LocalDateTime.now())
                            .orElse(null);
                    return ItemMapper.toOwnerDto(item, lastBooking, nextBooking, comments);
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemDto> searchItems(String text) {
        if (text == null || text.isEmpty()) {
            return List.of();
        }
        String lowerText = text.toLowerCase();
        List<Item> items = itemRepository.findAll();
        return items.stream()
                .filter(item -> item.isAvailable() &&
                                (item.getName().toLowerCase().contains(lowerText) ||
                                 item.getDescription().toLowerCase().contains(lowerText)))
                .map(item -> {
                    List<CommentDto> comments = commentRepository.findByItemIdOrderByCreatedAsc(item.getId())
                            .stream().map(CommentMapper::toDto).collect(Collectors.toList());
                    return ItemMapper.toDto(item, comments);
                })
                .collect(Collectors.toList());
    }

    @Override
    public CommentDto addComment(Long itemId, Long userId, CommentDto commentDto) {
        Item item = getItemById(itemId);
        User author = getUserById(userId);
        boolean hasPastBooking = bookingRepository.findByBookerIdOrderByStartTimeDesc(userId).stream()
                .anyMatch(booking ->
                        booking.getItem().getId().equals(itemId) &&
                        booking.getEndTime().isBefore(LocalDateTime.now()) &&
                        booking.getStatus() == BookingStatus.APPROVED);
        if (!hasPastBooking) {
            throw new IllegalArgumentException("Пользователь не брал вещь в аренду или аренда ещё не завершена");
        }
        var comment = CommentMapper.toEntity(commentDto, item, author);
        commentRepository.save(comment);
        return CommentMapper.toDto(comment);
    }

    private User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));
    }

    private Item getItemById(Long id) {
        return itemRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Вещь не найдена"));
    }
}