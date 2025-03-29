package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.DuplicateEmailException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserMapper;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public UserDto addUser(UserDto userDto) {
        log.info("Called addUser(userDto={})", userDto);

        if (userDto.getEmail() != null && userRepository.findUserByEmail(userDto.getEmail()).isPresent()) {
            log.warn("Email {} is already used by another user. Throwing DuplicateEmailException.", userDto.getEmail());
            throw new DuplicateEmailException("Пользователь с таким email уже существует");
        }
        User user = new User();
        user.setName(userDto.getName());
        user.setEmail(userDto.getEmail());
        userRepository.save(user);
        log.debug("User saved: {}", user);

        return UserMapper.toDto(user);
    }

    @Override
    public UserDto updateUser(Long userId, UserDto userDto) {
        log.info("Called updateUser(userId={}, userDto={})", userId, userDto);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("User with id={} not found for update", userId);
                    return new NoSuchElementException("Пользователь не найден");
                });
        if (userDto.getName() != null) {
            user.setName(userDto.getName());
        }
        if (userDto.getEmail() != null && !userDto.getEmail().equalsIgnoreCase(user.getEmail())) {
            Optional<User> duplicate = userRepository.findUserByEmail(userDto.getEmail());
            if (duplicate.isPresent() && !duplicate.get().getId().equals(userId)) {
                log.warn("Email {} is already used by another user {}. Throwing DuplicateEmailException.",
                        userDto.getEmail(), duplicate.get().getId());
                throw new DuplicateEmailException("Пользователь с таким email уже существует");
            }
            user.setEmail(userDto.getEmail());
        }
        userRepository.save(user);

        log.debug("User {} updated to: {}", userId, user);
        return UserMapper.toDto(user);
    }

    @Override
    public UserDto getUser(Long userId) {
        log.info("Called getUser(userId={})", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("User with id={} not found", userId);
                    return new NoSuchElementException("Пользователь не найден");
                });
        log.debug("Found user: {}", user);
        return UserMapper.toDto(user);
    }

    @Override
    public List<UserDto> getUsers() {
        log.info("Called getUsers()");
        List<User> all = userRepository.findAll();
        log.debug("Found {} users total", all.size());
        return all.stream()
                .map(UserMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteUser(Long userId) {
        log.info("Called deleteUser(userId={})", userId);
        userRepository.deleteById(userId);
        log.debug("User {} deleted (if existed).", userId);
    }
}