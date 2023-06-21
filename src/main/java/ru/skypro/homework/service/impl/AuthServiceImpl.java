package ru.skypro.homework.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.JdbcUserDetailsManager;
import org.springframework.stereotype.Service;
import ru.skypro.homework.dto.RegisterReqDto;
import ru.skypro.homework.dto.Role;
import ru.skypro.homework.service.AuthService;
import ru.skypro.homework.service.UserService;

@Service
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final JdbcUserDetailsManager manager;

    private final PasswordEncoder encoder;

    private final UserService userService;


    public AuthServiceImpl(JdbcUserDetailsManager manager, PasswordEncoder passwordEncoder, UserService userService) {
        this.manager = manager;
        this.encoder = passwordEncoder;
        this.userService = userService;
    }

    /**
     * Login
     *
     * @param userName username
     * @param password password
     * @return true - success login, false - login failed
     */
    @Override
    public boolean login(String userName, String password) {
        log.info("login user: " + userName);
        if (!manager.userExists(userName)) {
            return false;
        }
        UserDetails userDetails = manager.loadUserByUsername(userName);
        return encoder.matches(password, userDetails.getPassword());
    }

    /**
     * Register new user
     *
     * @param registerReq register data
     * @param role        role
     * @return true - success register, false - register failed
     */
    @Override
    public boolean register(RegisterReqDto registerReq, Role role) {
        log.info("register new user");
        if (manager.userExists(registerReq.getUsername())) {
            return false;
        }
        userService.create(registerReq, role);
        return true;
    }
}
