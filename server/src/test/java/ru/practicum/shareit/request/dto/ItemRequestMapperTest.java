package ru.practicum.shareit.request.dto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.user.User;

class ItemRequestMapperTest {

    @Test
    void testToDto_andToEntity() {
        User requestor = new User(1L, "Test User", "test@example.com");
        LocalDateTime now = LocalDateTime.now();
        ItemRequest request = new ItemRequest(10L, "Test Request", requestor, now);

        ItemRequestDto dto = ItemRequestMapper.toDto(request);
        assertThat(dto).isNotNull();
        assertThat(dto.getId()).isEqualTo(10L);
        assertThat(dto.getDescription()).isEqualTo("Test Request");
        assertThat(dto.getRequestorId()).isEqualTo(1L);
        assertThat(dto.getCreated()).isEqualTo(now);

        ItemRequest entity = ItemRequestMapper.toEntity(dto, requestor);
        assertThat(entity).isNotNull();
        assertThat(entity.getId()).isEqualTo(dto.getId());
        assertThat(entity.getDescription()).isEqualTo(dto.getDescription());
        assertThat(entity.getRequestor().getId()).isEqualTo(dto.getRequestorId());
        assertThat(entity.getCreated()).isEqualTo(dto.getCreated());
    }

    @Test
    void testToDto_null() {
        ItemRequestDto dto = ItemRequestMapper.toDto(null);
        assertNull(dto);
    }

    @Test
    void testToEntity_null() {
        ItemRequest entity = ItemRequestMapper.toEntity(null, new User());
        assertNull(entity);
    }
}