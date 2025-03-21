package ru.practicum.shareit.item;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;

import java.util.List;

@RestController
@RequestMapping("/items")
public class ItemController {

    private final ItemService itemService;

    public ItemController(ItemService itemService) {
        this.itemService = itemService;
    }

    // Добавление новой вещи
    @PostMapping
    public ItemDto addItem(@Valid @RequestBody ItemDto itemDto,
                           @RequestHeader("X-Sharer-User-Id") Long ownerId) {
        return itemService.addItem(itemDto, ownerId);
    }

    // Редактирование вещи (только владелец)
    @PatchMapping("/{itemId}")
    public ItemDto updateItem(@PathVariable Long itemId,
                              @RequestBody ItemDto itemDto,
                              @RequestHeader("X-Sharer-User-Id") Long ownerId) {
        return itemService.updateItem(itemId, itemDto, ownerId);
    }

    // Получение информации о конкретной вещи
    @GetMapping("/{itemId}")
    public ItemDto getItemById(@PathVariable Long itemId,
                               @RequestHeader("X-Sharer-User-Id") Long requesterId) {
        return itemService.getItemById(itemId, requesterId);
    }

    // Получение списка всех вещей владельца
    @GetMapping
    public List<ItemDto> getItemsByOwner(@RequestHeader("X-Sharer-User-Id") Long ownerId) {
        return itemService.getItemsByOwner(ownerId);
    }

    // Поиск вещей по тексту (возвращаются только доступные для аренды)
    @GetMapping("/search")
    public List<ItemDto> searchItems(@RequestParam String text) {
        return itemService.searchItems(text);
    }

    //
    @PostMapping("/{itemId}/comment")
    public CommentDto addComment(@PathVariable Long itemId,
                                 @RequestHeader("X-Sharer-User-Id") Long userId,
                                 @RequestBody CommentDto commentDto) {
        return itemService.addComment(itemId, userId, commentDto);
    }
}
