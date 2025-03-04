package ru.practicum.shareit.item;

import ru.practicum.shareit.item.dto.ItemDto;

import java.util.List;

public interface ItemService {
    ItemDto addItem(ItemDto itemDto, Long ownerId);
    ItemDto updateItem(Long itemId, ItemDto itemDto, Long ownerId);
    ItemDto getItemById(Long itemId, Long requesterId);
    List<ItemDto> getItemsByOwner(Long ownerId);
    List<ItemDto> searchItems(String text);
}
