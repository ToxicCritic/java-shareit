package ru.practicum.shareit.user;

public interface UserRepository {
    User findById(Long id);
}