package ru.skypro.homework.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.JdbcUserDetailsManager;
import ru.skypro.homework.dto.RegisterReqDto;
import ru.skypro.homework.dto.SecuringUserDto;
import ru.skypro.homework.model.Role;
import ru.skypro.homework.security.UserDetailsImpl;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {
    @InjectMocks
    private AuthService authService;
    @Mock
    private JdbcUserDetailsManager manager;
    @Mock
    private PasswordEncoder encoder;
    @Mock
    private UserService userService;

    private UserDetailsImpl userDetails;

    @BeforeEach
    public void setup() {
        SecuringUserDto user = new SecuringUserDto();
        System.out.println(user.getPassword());
        System.out.println(user.getUsername());
        userDetails = new UserDetailsImpl();
        userDetails.setUser(user);
    }

    @Test
    public void login() {
        doReturn(true).when(manager).userExists(any());
        doReturn(userDetails).when(manager).loadUserByUsername(any());
        doReturn(true).when(encoder).matches(any(), any());
        boolean isTrueLogin = authService.login(userDetails.getUsername(), userDetails.getPassword());
        assertTrue(isTrueLogin);
    }

    @Test
    public void register() {
        RegisterReqDto reqDto = new RegisterReqDto();
        doReturn(false).when(manager).userExists(any());
        doNothing().when(userService).create(any(),any());
        boolean isTrueRegister = authService.register(reqDto, Role.USER);
        assertTrue(isTrueRegister);
    }
}
