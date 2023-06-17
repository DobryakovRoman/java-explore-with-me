package ru.practicum.user.service;

import org.springframework.stereotype.Component;
import ru.practicum.user.dto.NewUserRequest;
import ru.practicum.user.dto.UserDto;
import ru.practicum.user.dto.UserShortDto;
import ru.practicum.user.model.User;

@Component
public class UserDtoMapper {

    public static UserDto mapUserToDto(User user) {
        return new UserDto(
                user.getId(),
                user.getEmail(),
                user.getName()
        );
    }

    public static UserShortDto mapUserToShortDto(User user) {
        if (user.getId() == null) {
            return UserShortDto.builder()
                    .id(null)
                    .name(user.getName())
                    .build();
        }
        return new UserShortDto(user.getId(), user.getName());
    }

    public User mapDtoToUser(UserDto userDto) {
        return new User(
                userDto.getId(),
                userDto.getEmail(),
                userDto.getName()
        );
    }

    public static User mapNewUserRequestToUser(NewUserRequest newUser) {
        return new User(
                null,
                newUser.getEmail(),
                newUser.getName()
        );
    }
}
