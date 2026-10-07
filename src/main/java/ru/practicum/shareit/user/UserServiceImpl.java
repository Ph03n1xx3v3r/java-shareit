package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;

import java.util.List;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");

    private final UserStorage userStorage;

    @Override
    public UserDto create(UserDto userDto) {
        validateName(userDto.getName());
        validateEmail(userDto.getEmail());
        checkEmailUnique(userDto.getEmail(), null);

        User savedUser = userStorage.create(UserMapper.toUser(userDto));
        return UserMapper.toUserDto(savedUser);
    }

    @Override
    public UserDto update(long userId, UserDto userDto) {
        User user = getUser(userId);

        if (userDto.getName() != null) {
            validateName(userDto.getName());
            user.setName(userDto.getName());
        }

        if (userDto.getEmail() != null) {
            validateEmail(userDto.getEmail());
            checkEmailUnique(userDto.getEmail(), userId);
            user.setEmail(userDto.getEmail());
        }

        return UserMapper.toUserDto(userStorage.update(user));
    }

    @Override
    public UserDto getById(long userId) {
        return UserMapper.toUserDto(getUser(userId));
    }

    @Override
    public User getUser(long userId) {
        return userStorage.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id=" + userId + " не найден"));
    }

    @Override
    public List<UserDto> getAll() {
        return userStorage.findAll().stream()
                .map(UserMapper::toUserDto)
                .toList();
    }

    @Override
    public void delete(long userId) {
        userStorage.deleteById(userId);
    }

    private void checkEmailUnique(String email, Long excludedUserId) {
        if (userStorage.existsByEmail(email, excludedUserId)) {
            throw new ConflictException("Пользователь с email " + email + " уже существует");
        }
    }

    private void validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new ValidationException("Имя пользователя не может быть пустым");
        }
    }

    private void validateEmail(String email) {
        if (email == null || email.isBlank() || !EMAIL_PATTERN.matcher(email).matches()) {
            throw new ValidationException("Некорректный email");
        }
    }
}
