package ru.practicum.shareit.item;

import org.springframework.stereotype.Repository;
import ru.practicum.shareit.item.model.Item;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class InMemoryItemRepository implements ItemRepository {
    private final Map<Long, Item> itemStorage = new HashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    @Override
    public Item findById(Long id) {
        return itemStorage.get(id);
    }

    @Override
    public void save(Item item) {
        if (item.getId() == null) {
            item.setId(idGenerator.getAndIncrement());
        }
        itemStorage.put(item.getId(), item);
    }

    @Override
    public Collection<Item> findAll() {
        return itemStorage.values();
    }

    @Override
    public void delete(Long id) {
        itemStorage.remove(id);
    }
}