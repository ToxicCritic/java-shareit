package ru.practicum.shareit.user;

import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;

@Repository
public class InMemoryUserRepository implements UserRepository {
    private final Map<Long, User> userStorage = new HashMap<>();

    @Override
    public User findById(Long id) {
        return userStorage.get(id);
    }

    public void save(User user) {
        userStorage.put(user.getId(), user);
    }
}
