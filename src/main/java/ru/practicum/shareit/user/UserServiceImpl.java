package ru.practicum.shareit.user;

import org.springframework.stereotype.Service;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserMapper;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    // Хранилище пользователей в памяти
    private final Map<Long, User> userStorage = new HashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    @Override
    public UserDto addUser(UserDto userDto) {
        // Проверка уникальности email
        for (User user : userStorage.values()) {
            if (user.getEmail().equalsIgnoreCase(userDto.getEmail())) {
                throw new IllegalArgumentException("Пользователь с таким email уже существует");
            }
        }
        User user = new User();
        user.setId(idGenerator.getAndIncrement());
        user.setName(userDto.getName());
        user.setEmail(userDto.getEmail());
        userStorage.put(user.getId(), user);
        return UserMapper.toUserDto(user);
    }

    @Override
    public UserDto updateUser(Long userId, UserDto userDto) {
        User user = userStorage.get(userId);
        if (user == null) {
            throw new NoSuchElementException("Пользователь не найден");
        }
        if (userDto.getName() != null) {
            user.setName(userDto.getName());
        }
        if (userDto.getEmail() != null && !userDto.getEmail().equalsIgnoreCase(user.getEmail())) {
            // Проверка уникальности email
            for (User u : userStorage.values()) {
                if (u.getEmail().equalsIgnoreCase(userDto.getEmail())) {
                    throw new IllegalArgumentException("Пользователь с таким email уже существует");
                }
            }
            user.setEmail(userDto.getEmail());
        }
        return UserMapper.toUserDto(user);
    }

    @Override
    public UserDto getUser(Long userId) {
        User user = userStorage.get(userId);
        if (user == null) {
            throw new NoSuchElementException("Пользователь не найден");
        }
        return UserMapper.toUserDto(user);
    }

    @Override
    public List<UserDto> getUsers() {
        return userStorage.values().stream()
                .map(UserMapper::toUserDto)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteUser(Long userId) {
        userStorage.remove(userId);
    }
}
