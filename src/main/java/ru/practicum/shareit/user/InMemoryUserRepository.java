package ru.practicum.shareit.user;

import org.springframework.stereotype.Repository;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class InMemoryUserRepository implements UserRepository {
    private final Map<Long, User> userStorage = new HashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    @Override
    public User findById(Long id) {
        return userStorage.get(id);
    }

    @Override
    public void save(User user) {
        if (user.getId() == null) {
            user.setId(idGenerator.getAndIncrement());
        }
        userStorage.put(user.getId(), user);
    }

    @Override
    public Collection<User> findAll() {
        return userStorage.values();
    }

    @Override
    public void delete(Long id) {
        userStorage.remove(id);
    }

    @Override
    public Optional<User> findUserByEmail(String email) {
        return userStorage.values().stream()
                .filter(user -> user.getEmail().equalsIgnoreCase(email))
                .findFirst();
    }
}