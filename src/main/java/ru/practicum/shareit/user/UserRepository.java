package ru.practicum.shareit.user;

import java.util.Collection;

public interface UserRepository {
    User findById(Long id);

    void save(User user);

    Collection<User> findAll();

    void delete(Long id);
}