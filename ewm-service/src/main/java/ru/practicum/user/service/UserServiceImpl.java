package ru.practicum.user.service;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.user.dto.NewUserRequest;
import ru.practicum.user.dto.UserDto;
import ru.practicum.user.model.User;
import ru.practicum.user.repository.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserServiceImpl implements UserService {

    final UserRepository userRepository;

    @Override
    public List<UserDto> getUsers(List<Long> ids, Integer from, Integer size) {
        if (ids == null) {
            return userRepository.findAllPageable(PageRequest.of(from / size, size)).stream()
                    .map(UserDtoMapper::mapUserToDto)
                    .collect(Collectors.toList());
        }
        return userRepository.findAllByIdsPageable(ids, PageRequest.of(from / size, size)).stream()
                .map(UserDtoMapper::mapUserToDto)
                .collect(Collectors.toList());
    }

    @Override
    public User getUserById(Long userId) {
        return userRepository.findById(userId).orElseThrow(
                () -> new NotFoundException("Пользователь с id " + userId + " не найден"));
    }

    @Override
    public UserDto addUser(NewUserRequest newUserDto) {
        if (userRepository.countByName(newUserDto.getName()) > 0) {
            throw new ConflictException("Пользователь " + newUserDto.getName() + " уже существует");
        }
        User savedUser = userRepository.save(UserDtoMapper.mapNewUserRequestToUser(newUserDto));
        log.info("Пользователь создан, " + savedUser.getId());
        return UserDtoMapper.mapUserToDto(savedUser);
    }

    @Override
    public void deleteUser(Long userId) {
        userRepository.deleteById(userId);
        log.info("Пользователь удалён");
    }
}
