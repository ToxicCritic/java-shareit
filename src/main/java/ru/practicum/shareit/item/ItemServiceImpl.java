package ru.practicum.shareit.item;

import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingRepository;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;

    public ItemServiceImpl(ItemRepository itemRepository, UserRepository userRepository, BookingRepository bookingRepository) {
        this.itemRepository = itemRepository;
        this.userRepository = userRepository;
        this.bookingRepository = bookingRepository;
    }

    @Override
    public ItemDto addItem(ItemDto itemDto, Long ownerId) {
        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));
        Item item = new Item();
        item.setName(itemDto.getName());
        item.setDescription(itemDto.getDescription());
        item.setAvailable(itemDto.getAvailable());
        item.setOwner(owner);
        itemRepository.save(item);
        return ItemMapper.toDto(item);
    }

    @Override
    public ItemDto updateItem(Long itemId, ItemDto itemDto, Long ownerId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Вещь не найдена"));
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
        return ItemMapper.toDto(item);
    }

    @Override
    public ItemDto getItemById(Long itemId, Long requesterId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Вещь не найдена"));
        if (item.getOwner().getId().equals(requesterId)) {
            Booking lastBooking = bookingRepository.findFirstByItemIdAndStartBeforeOrderByStartDesc(itemId, LocalDateTime.now()).orElse(null);
            Booking nextBooking = bookingRepository.findFirstByItemIdAndStartAfterOrderByStartAsc(itemId, LocalDateTime.now()).orElse(null);
            return ItemMapper.toOwnerDto(item, lastBooking, nextBooking);
        }
        return ItemMapper.toDto(item);
    }

    @Override
    public List<ItemDto> getItemsByOwner(Long ownerId) {
        Collection<Item> items = itemRepository.findByOwnerId(ownerId);
        return items.stream()
                .map(item -> {
                    Booking lastBooking = bookingRepository.findFirstByItemIdAndStartBeforeOrderByStartDesc(item.getId(), LocalDateTime.now()).orElse(null);
                    Booking nextBooking = bookingRepository.findFirstByItemIdAndStartAfterOrderByStartAsc(item.getId(), LocalDateTime.now()).orElse(null);
                    return ItemMapper.toOwnerDto(item, lastBooking, nextBooking);
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemDto> searchItems(String text) {
        if (text == null || text.isEmpty()) {
            return List.of();
        }
        String lowerText = text.toLowerCase();
        Collection<Item> items = itemRepository.findAll();
        return items.stream()
                .filter(item -> item.isAvailable() &&
                                (item.getName().toLowerCase().contains(lowerText) ||
                                 item.getDescription().toLowerCase().contains(lowerText)))
                .map(ItemMapper::toDto)
                .collect(Collectors.toList());
    }
}