package ru.practicum.shareit.user;

import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class InMemoryUserStorage implements UserStorage {
    private final Map<Long, User> users = new LinkedHashMap<>();
    private long nextId = 1;

    @Override
    public User create(User user) {
        user.setId(nextId++);
        users.put(user.getId(), user);
        return user;
    }

    @Override
    public User update(User user) {
        users.put(user.getId(), user);
        return user;
    }

    @Override
    public Optional<User> findById(long userId) {
        return Optional.ofNullable(users.get(userId));
    }

    @Override
    public List<User> findAll() {
        return new ArrayList<>(users.values());
    }

    @Override
    public void deleteById(long userId) {
        users.remove(userId);
    }

    @Override
    public boolean existsByEmail(String email, Long excludedUserId) {
        return users.values().stream()
                .anyMatch(user -> user.getEmail().equalsIgnoreCase(email)
                        && (excludedUserId == null || !user.getId().equals(excludedUserId)));
    }
}
