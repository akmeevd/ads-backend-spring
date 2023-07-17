package ru.skypro.homework.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.JdbcUserDetailsManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import ru.skypro.homework.component.AuthenticationComponent;
import ru.skypro.homework.dto.NewPasswordDto;
import ru.skypro.homework.dto.RegisterReqDto;
import ru.skypro.homework.exception.ImageUploadException;
import ru.skypro.homework.model.Image;
import ru.skypro.homework.model.Role;
import ru.skypro.homework.dto.UserDto;
import ru.skypro.homework.exception.UserNotFoundException;
import ru.skypro.homework.exception.UserUnauthorizedException;
import ru.skypro.homework.mapper.UserMapper;
import ru.skypro.homework.model.User;
import ru.skypro.homework.repository.UserRepository;
import ru.skypro.homework.dto.SecuringUserDto;
import ru.skypro.homework.security.UserDetailsImpl;

import java.io.IOException;

/**
 * Service for maintain users via {@link UserRepository}
 */
@Service
@Slf4j
public class UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final JdbcUserDetailsManager manager;
    private final PasswordEncoder encoder;
    private final ImageService imageService;
    private final AuthenticationComponent auth;

    public UserService(UserRepository userRepository,
                       UserMapper userMapper,
                       JdbcUserDetailsManager manager,
                       PasswordEncoder encoder,
                       ImageService imageService,
                       AuthenticationComponent auth) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.manager = manager;
        this.encoder = encoder;
        this.imageService = imageService;
        this.auth = auth;
    }

    /**
     * Create user via {@link UserRepository}
     *
     * @param reqDto {@link RegisterReqDto}
     * @param role   {@link Role}
     */
    @Transactional
    public void create(RegisterReqDto reqDto, Role role) {
        log.info("Create new user");
        SecuringUserDto securingUserDto = new SecuringUserDto(reqDto.getUsername(),
                encoder.encode(reqDto.getPassword()), role, true);
        UserDetails userDetails = new UserDetailsImpl(securingUserDto);
        manager.createUser(userDetails);
        update(reqDto, role);
    }

    /**
     * Update user info after register via {@link UserRepository}
     *
     * @param reqDto {@link RegisterReqDto}
     * @param role {@link Role}
     */
    @Transactional
    public void update(RegisterReqDto reqDto, Role role) {
        log.info("Update user info after creation");
        User user = userRepository.findByUsername(reqDto.getUsername());
        if (user == null) {
            log.error("User not found");
            throw new UserUnauthorizedException("User not found");
        }
        userMapper.updateUser(reqDto, user);
        user.setRole(role);
        userRepository.save(user);
    }

    /**
     * Update user info via {@link UserRepository}
     *
     * @param userDto {@link UserDto}
     * @return {@link UserDto}
     */
    @Transactional
    public UserDto update(UserDto userDto) {
        log.info("Update user info: " + userDto);
        User user = userRepository.findByUsername(auth.getAuth().getName());
        if (user == null) {
            log.error("User not found");
            throw new UserUnauthorizedException("User not found");
        }
        userMapper.updateUser(userDto, user);
        userRepository.save(user);
        return userMapper.userToUserDto(user);
    }

    /**
     * Change user password
     *
     * @param newPassword {@link NewPasswordDto}
     */
    @Transactional
    public void setPassword(NewPasswordDto newPassword) {
        log.info("Set new password");
        manager.changePassword(newPassword.getCurrentPassword(), encoder.encode(newPassword.getNewPassword()));
    }

    /**
     * Update avatar of authorized user
     *
     * @param file {@link MultipartFile}
     * @return bytes
     */
    @Transactional
    public byte[] updateImage(MultipartFile file) {
        log.info("Update user avatar");
        try {
            User user = userRepository.findByUsername(auth.getAuth().getName());
            imageService.uploadAvatar(user, file);
            return file.getBytes();
        } catch (IOException exception) {
            log.error(exception.getMessage());
            throw new ImageUploadException(exception.getMessage());
        }
    }

    /**
     * Download avatar of authorized user
     *
     * @return {@link Image}
     */
    public Image downloadImage() {
        log.info("Download user avatar with email: " + auth.getAuth().getName());
        User user = userRepository.findByUsername(auth.getAuth().getName());
        return user.getAvatar();
    }

    /**
     * Download avatar by user id
     *
     * @param id user id
     * @return {@link Image}
     */
    public Image downloadImageByUserId(int id) {
        log.info("Download user image with id: " + id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        return user.getAvatar();
    }

    /**
     * Get user info via {@link UserRepository}
     *
     * @return {@link UserDto}
     */
    public UserDto findInfo() {
        User user = userRepository.findByUsername(auth.getAuth().getName());
        return userMapper.userToUserDto(user);
    }
}
