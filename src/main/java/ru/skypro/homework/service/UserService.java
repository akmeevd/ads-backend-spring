package ru.skypro.homework.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.JdbcUserDetailsManager;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.stereotype.Service;
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
public class UserService{
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
        User user = userRepository.findByEmail(auth.getName());
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
    public byte[] updateAvatar(Authentication auth, MultipartFile image) throws IOException {
        log.info("update user image");
        User user = userRepository.findByEmail(auth.getName());
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
        User user = userRepository.findByEmail(authentication.getName());
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
        User user = userRepository.findByEmail(auth.getName());
        return userMapper.userToUserDto(user);
    }

    public void createUser(RegisterReqDto reqDto, Role role) {
        User user = new User();
        user.setEmail(reqDto.getUsername());
        user.setPassword(encoder.encode(reqDto.getPassword()));
        user.setRole(role);
        manager.createUser(user);
        updateUser(reqDto,role);
    }

    public void updateUser(RegisterReqDto reqDto, Role role) {
        User user = userRepository.findByEmail(reqDto.getUsername());
        user.setPhone(reqDto.getPhone());
        user.setFirstName(reqDto.getFirstName());
        user.setLastName(reqDto.getLastName());
        user.setRole(role);
        userRepository.save(user);
    }
}
