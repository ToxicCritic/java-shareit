package ru.practicum.shareit.user.dto;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.user.User;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNull;

public class UserMapperTest {

    @Test
    public void testToDto() {
        User user = new User();
        user.setId(1L);
        user.setName("Test User");
        user.setEmail("test@example.com");

        UserDto dto = UserMapper.toDto(user);

        assertThat(dto).isNotNull();
        assertThat(dto.getId()).isEqualTo(user.getId());
        assertThat(dto.getName()).isEqualTo(user.getName());
        assertThat(dto.getEmail()).isEqualTo(user.getEmail());
    }

    @Test
    void testToDto_nullInput() {
        UserDto dto = UserMapper.toDto(null);
        assertNull(dto);
    }

    @Test
    void testToEntity() {
        UserDto dto = new UserDto(1L, "Test User", "test@example.com");

        User user = UserMapper.toEntity(dto);

        assertThat(user).isNotNull();
        assertThat(user.getId()).isEqualTo(dto.getId());
        assertThat(user.getName()).isEqualTo(dto.getName());
        assertThat(user.getEmail()).isEqualTo(dto.getEmail());
    }

    @Test
    void testToEntity_nullInput() {
        User user = UserMapper.toEntity(null);
        assertNull(user);
    }
}