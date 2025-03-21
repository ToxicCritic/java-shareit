package ru.practicum.shareit.item;

import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;
import java.util.Collection;
import java.util.stream.Collectors;

@Service
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;
    private final UserRepository userRepository;

    public ItemServiceImpl(ItemRepository itemRepository, UserRepository userRepository) {
        this.itemRepository = itemRepository;
        this.userRepository = userRepository;
    }

    @Override
    public ItemDto addItem(ItemDto itemDto, Long ownerId) {
        User owner = userRepository.findById(ownerId);
        if (owner == null) {
            throw new NotFoundException("Пользователь не найден");
        }
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
        Item item = itemRepository.findById(itemId);
        if (item == null) {
            throw new NotFoundException("Вещь не найдена");
        }
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
        Item item = itemRepository.findById(itemId);
        if (item == null) {
            throw new NotFoundException("Вещь не найдена");
        }
        return ItemMapper.toDto(item);
    }

    @Override
    public java.util.List<ItemDto> getItemsByOwner(Long ownerId) {
        Collection<Item> items = itemRepository.findAll();
        return items.stream()
                .filter(item -> item.getOwner().getId().equals(ownerId))
                .map(ItemMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public java.util.List<ItemDto> searchItems(String text) {
        if (text == null || text.isEmpty()) {
            return java.util.Collections.emptyList();
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