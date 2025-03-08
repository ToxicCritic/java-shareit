package ru.practicum.shareit.item;

import ru.practicum.shareit.item.model.Item;

import java.util.Collection;

public interface ItemRepository {
    Item findById(Long id);

    void save(Item item);

    Collection<Item> findAll();

    void delete(Long id);
}