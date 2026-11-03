package ru.practicum.shareit.user.service;

import ru.practicum.shareit.user.dto.UserDto;

import java.util.Map;

public interface UserService {
    UserDto createUser(UserDto userDto);

    UserDto updateUser(Map<String, String> updates, Long id);

    Map<String, String> deleteUser(Long id);

    UserDto getUserById(Long id);

    public void checkObjectOwnerAndGetUserEntityById(Long id);
}