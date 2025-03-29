package ru.practicum.shareit.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.DuplicateEmailException;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;
import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Transactional
class UserServiceImplTest {

    @Autowired
    private UserService userService;

    @BeforeEach
    void setUp() {
    }

    @Test
    void testAddUser() {
        UserDto input = new UserDto(null, "John Doe", "john@example.com");
        UserDto saved = userService.addUser(input);
        assertThat(saved).isNotNull();
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getName()).isEqualTo("John Doe");
        assertThat(saved.getEmail()).isEqualTo("john@example.com");
    }

    @Test
    void testAddUserDuplicateEmail() {
        UserDto input = new UserDto(null, "John Doe", "john@example.com");
        userService.addUser(input);

        DuplicateEmailException exception = assertThrows(DuplicateEmailException.class, () ->
                userService.addUser(new UserDto(null, "Jane Doe", "john@example.com")));
        assertThat(exception.getMessage()).contains("Пользователь с таким email уже существует");
    }

    @Test
    void testUpdateUser() {
        UserDto input = new UserDto(null, "John Doe", "john@example.com");
        UserDto saved = userService.addUser(input);

        UserDto update = new UserDto(null, "Johnny", "johnny@example.com");
        UserDto updated = userService.updateUser(saved.getId(), update);
        assertThat(updated.getName()).isEqualTo("Johnny");
        assertThat(updated.getEmail()).isEqualTo("johnny@example.com");
    }

    @Test
    void testGetUser() {
        UserDto input = new UserDto(null, "John Doe", "john@example.com");
        UserDto saved = userService.addUser(input);

        UserDto fetched = userService.getUser(saved.getId());
        assertThat(fetched).isNotNull();
        assertThat(fetched.getId()).isEqualTo(saved.getId());
        assertThat(fetched.getName()).isEqualTo("John Doe");
    }

    @Test
    void testGetUsers() {
        userService.addUser(new UserDto(null, "User1", "user1@example.com"));
        userService.addUser(new UserDto(null, "User2", "user2@example.com"));
        List<UserDto> users = userService.getUsers();
        assertThat(users).hasSizeGreaterThanOrEqualTo(2);
    }

    @Test
    void testDeleteUser() {
        UserDto input = new UserDto(null, "John Doe", "john@example.com");
        UserDto saved = userService.addUser(input);
        userService.deleteUser(saved.getId());
        assertThrows(NoSuchElementException.class, () -> userService.getUser(saved.getId()));
    }
}