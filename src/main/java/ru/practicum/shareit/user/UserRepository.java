package ru.practicum.shareit.user;

import java.util.Collection;
import java.util.Optional;

public interface UserRepository {
    User findById(Long id);

    void save(User user);

    Collection<User> findAll();

    void delete(Long id);

    Optional<User> findUserByEmail(String email);
}