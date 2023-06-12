package ru.skypro.homework.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.skypro.homework.dto.NewPasswordDto;
import ru.skypro.homework.dto.UserDto;
import ru.skypro.homework.exception.UserUnauthorizedException;
import ru.skypro.homework.mapper.UserMapper;
import ru.skypro.homework.model.User;
import ru.skypro.homework.repository.UserRepository;

/**
 * service for maintain users via {@link UserRepository}
 */
@Service
@Slf4j
public class UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final UserDetailsManager manager;
    private final PasswordEncoder encoder;

    public UserService(UserRepository userRepository, UserMapper userMapper,
                       UserDetailsManager manager, PasswordEncoder encoder) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.manager = manager;
        this.encoder = encoder;
    }

    /**
     * Change user password
     *
     * @param auth        authorized user
     * @param newPassword new password
     */
    public void setPassword(Authentication auth, NewPasswordDto newPassword) {
        log.info("set new password");
        manager.changePassword(newPassword.getCurrentPassword(), encoder.encode(newPassword.getNewPassword()));
    }

    /**
     * Update user info via {@link UserRepository}
     *
     * @param auth    authorized user
     * @param userDto user DTO object
     * @return user DTO object
     */
    public UserDto updateInfo(Authentication auth, UserDto userDto) {
        log.info("update user info: " + userDto);
        User user = userRepository.findByEmail(userDto.getEmail());
        if (user == null) {
            throw new UserUnauthorizedException("User not found");
        }
        userMapper.updateUser(userDto, user);
        userRepository.save(user);
        return userMapper.userToUserDto(user);
    }

    /**
     * Update user image
     *
     * @param auth  authorized user
     * @param image image
     */
    public void updateImage(Authentication auth, MultipartFile image) {
        log.info("update user image");
        //to be done
    }

    /**
     * Get user info via {@link UserRepository}
     *
     * @param auth authorized user
     * @return user DTO object
     */
    public UserDto findInfo(Authentication auth) {
        User user = userRepository.findByEmail(auth.getName());
        return userMapper.userToUserDto(user);
    }
}
