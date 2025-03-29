package ru.practicum.shareit.request.dto;

import ru.practicum.shareit.item.dto.ItemShortDto;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.user.User;

import java.util.List;

public class ItemRequestMapper {

    public static ItemRequestDto toDto(ItemRequest itemRequest, List<ItemShortDto> items) {
        if (itemRequest == null) {
            return null;
        }
        return new ItemRequestDto(
                itemRequest.getId(),
                itemRequest.getDescription(),
                (itemRequest.getRequestor() != null ? itemRequest.getRequestor().getId() : null),
                itemRequest.getCreated(),
                items
        );
    }

    public static ItemRequestDto toDto(ItemRequest itemRequest) {
        return toDto(itemRequest, List.of());
    }

    public static ItemRequest toEntity(ItemRequestDto dto, User requestor) {
        if (dto == null) {
            return null;
        }
        ItemRequest itemRequest = new ItemRequest();
        itemRequest.setId(dto.getId());
        itemRequest.setDescription(dto.getDescription());
        itemRequest.setRequestor(requestor);
        itemRequest.setCreated(dto.getCreated());
        return itemRequest;
    }
}