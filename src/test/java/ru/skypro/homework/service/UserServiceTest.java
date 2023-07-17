package ru.skypro.homework.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.JdbcUserDetailsManager;
import ru.skypro.homework.component.AuthenticationComponent;
import ru.skypro.homework.dto.NewPasswordDto;
import ru.skypro.homework.dto.RegisterReqDto;
import ru.skypro.homework.dto.UserDto;
import ru.skypro.homework.exception.UserUnauthorizedException;
import ru.skypro.homework.mapper.UserMapper;
import ru.skypro.homework.model.Avatar;
import ru.skypro.homework.model.Image;
import ru.skypro.homework.model.Role;
import ru.skypro.homework.model.User;
import ru.skypro.homework.repository.UserRepository;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {
    @InjectMocks
    private UserService userService;
    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JdbcUserDetailsManager jdbcUserDetailsManager;
    @Mock
    private AuthService authService;
    @Mock
    private AuthenticationComponent authenticationComponent;
    private User user;
    @Mock
    private Authentication authentication;
    @Mock
    private UserMapper userMapper;
    @Mock
    private ImageService imageService;
    private MockMultipartFile avatar;

    @BeforeEach
    public void setup() throws IOException {
        Path path = Path.of("src", "test\\resources\\picture\\images.jpeg");
        File file = new File(path.toUri());
        byte[] bytes = file.getAbsolutePath().getBytes();
        avatar = new MockMultipartFile("picture", bytes);
        user = new User();
        user.setId(1);
        user.setAvatar(new Avatar(path.toString()));

    }

    @Test
    public void create() {
        doReturn(user).when(userRepository).findByUsername(any());
        //doReturn(authentication).when(authenticationComponent).getAuth();
        RegisterReqDto reqDto = new RegisterReqDto();
        userService.create(reqDto, Role.USER);
        verify(userRepository, Mockito.times(1)).save(any());
    }

    @Test
    public void update() {
        doReturn(user).when(userRepository).findByUsername(any());
        //doReturn(authentication).when(authenticationComponent).getAuth();
        RegisterReqDto reqDto = new RegisterReqDto();
        userService.update(reqDto, Role.USER);
        verify(userRepository, Mockito.times(1)).save(any());
    }

    @Test
    public void updateAndReturnUserDto() {
        UserDto expectedUserDto = new UserDto();
        expectedUserDto.setId(1);
        doReturn(user).when(userRepository).findByUsername(any());
        doReturn(authentication).when(authenticationComponent).getAuth();
        doReturn(expectedUserDto).when(userMapper).userToUserDto(user);
        UserDto actualUserDto = userService.update(expectedUserDto);
        assertEquals(expectedUserDto, actualUserDto);
        verify(userRepository, Mockito.times(1)).save(any());
    }

    @Test
    public void setPassword() {
        NewPasswordDto newPasswordDto = new NewPasswordDto();
        userService.setPassword(newPasswordDto);
        verify(jdbcUserDetailsManager, Mockito.times(1)).changePassword(any(), any());
    }

    @Test
    public void updateAvatar() throws IOException {
        doReturn(user).when(userRepository).findByUsername(any());
        doReturn(authentication).when(authenticationComponent).getAuth();
        byte[] bytes  = userService.updateImage(avatar);
        verify(imageService, Mockito.times(1)).
                uploadAvatar(user, avatar);
        assertEquals(bytes, avatar.getBytes());
    }

    @Test
    public void downloadAvatar() {
        doReturn(user).when(userRepository).findByUsername(any());
        doReturn(authentication).when(authenticationComponent).getAuth();
        Image avatar = userService.downloadImage();
        assertNotNull(avatar);
    }

    @Test
    public void downloadAvatarByUserId() {
        when(userRepository.findById(any())).thenReturn(Optional.ofNullable(user));
        Image avatar = userService.downloadImageByUserId(user.getId());
        assertNotNull(avatar);
        assertEquals(avatar, user.getAvatar());
    }

    @Test
    public void findInfo() {
        UserDto expectedUserDto = new UserDto();
        doReturn(user).when(userRepository).findByUsername(any());
        doReturn(authentication).when(authenticationComponent).getAuth();
        doReturn(expectedUserDto).when(userMapper).userToUserDto(any());
        UserDto actualUserDto = userService.findInfo();
        Assertions.assertEquals(expectedUserDto, actualUserDto);
    }

    @Test
    public void doesThrowUserUnauthorizedException() {
        UserDto userDto = new UserDto();
        userDto.setId(101);
        RegisterReqDto reqDto = new RegisterReqDto();
        doReturn(null).when(userRepository).findByUsername(any());
        doReturn(authentication).when(authenticationComponent).getAuth();
        assertThrows(UserUnauthorizedException.class,
                () -> userService.update(userDto));
        assertThrows(UserUnauthorizedException.class,
                () -> userService.update(reqDto, Role.USER));
    }
}
