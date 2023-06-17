package ru.practicum.user.service;

import ru.practicum.user.dto.NewUserRequest;
import ru.practicum.user.dto.UserDto;
import ru.practicum.user.model.User;

import java.util.List;

public interface UserService {

    List<UserDto> getUsers(List<Long> ids, Integer from, Integer size);

    User getUserById(Long userId);

    UserDto addUser(NewUserRequest newUserDto);

    void deleteUser(Long userId);
}
