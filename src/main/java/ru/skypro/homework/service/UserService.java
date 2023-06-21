package ru.skypro.homework.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.JdbcUserDetailsManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import ru.skypro.homework.dto.NewPasswordDto;
import ru.skypro.homework.dto.RegisterReqDto;
import ru.skypro.homework.dto.Role;
import ru.skypro.homework.dto.UserDto;
import ru.skypro.homework.exception.UserNotFoundException;
import ru.skypro.homework.exception.UserUnauthorizedException;
import ru.skypro.homework.mapper.UserMapper;
import ru.skypro.homework.model.Avatar;
import ru.skypro.homework.model.User;
import ru.skypro.homework.repository.UserRepository;

import java.io.IOException;

/**
 * service for maintain users via {@link UserRepository}
 */
@Service
@Slf4j
public class UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final JdbcUserDetailsManager manager;
    private final PasswordEncoder encoder;
    private final PhotoService photoService;

    public UserService(UserRepository userRepository, UserMapper userMapper,
                       JdbcUserDetailsManager manager, PasswordEncoder encoder, PhotoService photoService) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.manager = manager;
        this.encoder = encoder;
        this.photoService = photoService;
    }

    /**
     * Create user
     *
     * @param reqDto user data
     * @param role   role
     */
    @Transactional
    public void create(RegisterReqDto reqDto, Role role) {
        log.info("create new user");
        User user = new User();
        user.setUsername(reqDto.getUsername());
        user.setPassword(encoder.encode(reqDto.getPassword()));
        user.setRole(role);
        user.setEnabled(true);
        manager.createUser(user);
        update(reqDto);
    }

    /**
     * Update user info
     *
     * @param reqDto user data
     */
    @Transactional
    public void update(RegisterReqDto reqDto) {
        log.info("update user info");
        User user = userRepository.findByUsername(reqDto.getUsername());
        if (user == null) {
            throw new UserUnauthorizedException("User not found");
        }
        userMapper.updateUser(reqDto, user);
        userRepository.save(user);
    }

    /**
     * Update user info via {@link UserRepository}
     *
     * @param auth    authorized user
     * @param userDto user DTO object
     * @return user DTO object
     */
    @Transactional
    public UserDto update(Authentication auth, UserDto userDto) {
        log.info("update user info: " + userDto);
        User user = userRepository.findByUsername(auth.getName());
        if (user == null) {
            throw new UserUnauthorizedException("User not found");
        }
        userMapper.updateUser(userDto, user);
        userRepository.save(user);
        return userMapper.userToUserDto(user);
    }

    /**
     * Change user password
     *
     * @param auth        authorized user
     * @param newPassword new password
     */
    @Transactional
    public void setPassword(Authentication auth, NewPasswordDto newPassword) {
        log.info("set new password");
        manager.changePassword(newPassword.getCurrentPassword(), encoder.encode(newPassword.getNewPassword()));
    }

    /**
     * Update user image
     *
     * @param auth  authorized user
     * @param image image
     */
    @Transactional
    public byte[] updateAvatar(Authentication auth, MultipartFile image) throws IOException {
        log.info("update user image");
        User user = userRepository.findByUsername(auth.getName());
        photoService.uploadAvatar(user, image);
        return image.getBytes();
    }

    /**
     * Download avatar authorized user
     *
     * @param authentication auth data
     * @return avatar object
     */
    public Avatar downloadAvatar(Authentication authentication) {
        log.info("Download user image with email: " + authentication.getName());
        User user = userRepository.findByUsername(authentication.getName());
        return user.getAvatar();
    }

    /**
     * Download avatar by user id
     *
     * @param id user id
     * @return avatar object
     */
    public Avatar downloadAvatarByUserId(int id) {
        log.info("Download user image with id: " + id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        return user.getAvatar();
    }

    /**
     * Get user info via {@link UserRepository}
     *
     * @param auth authorized user
     * @return user DTO object
     */
    public UserDto findInfo(Authentication auth) {
        User user = userRepository.findByUsername(auth.getName());
        return userMapper.userToUserDto(user);
    }
}
