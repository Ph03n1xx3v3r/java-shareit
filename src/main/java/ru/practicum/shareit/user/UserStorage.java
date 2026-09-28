package ru.practicum.shareit.user;

import java.util.List;
import java.util.Optional;

public interface UserStorage {
    User create(User user);

    User update(User user);

    Optional<User> findById(long userId);

    List<User> findAll();

    void deleteById(long userId);

    boolean existsByEmail(String email, Long excludedUserId);
}
