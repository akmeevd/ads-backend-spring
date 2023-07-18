package ru.skypro.homework.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.JdbcUserDetailsManager;
import org.springframework.stereotype.Service;
import ru.skypro.homework.dto.RegisterReqDto;
import ru.skypro.homework.model.Role;
import ru.skypro.homework.repository.AdvertRepository;

/**
 * Service provides auth functionality
 */
@Service
@Slf4j
public class AuthService {
    private final JdbcUserDetailsManager manager;
    private final PasswordEncoder encoder;
    private final UserService userService;

    public AuthService(JdbcUserDetailsManager manager,
                       PasswordEncoder passwordEncoder,
                       UserService userService) {
        this.manager = manager;
        this.encoder = passwordEncoder;
        this.userService = userService;
    }

    /**
     * Register new user
     *
     * @param registerReq {@link RegisterReqDto}
     * @param role        {@link Role}
     * @return true - success register, false - register failed
     */
    public boolean register(RegisterReqDto registerReq, Role role) {
        log.info("Register new user");
        if (manager.userExists(registerReq.getUsername())) {
            log.error("Register failed");
            return false;
        }
        userService.create(registerReq, role);
        return true;
    }

    /**
     * User login
     *
     * @param username username
     * @param password password
     * @return true - success login, false - login failed
     */
    public boolean login(String username, String password) {
        log.info("Login user: " + username);
        if (!manager.userExists(username)) {
            log.error("Login failed");
            return false;
        }
        UserDetails userDetails = manager.loadUserByUsername(username);
        return encoder.matches(password, userDetails.getPassword());
    }
}
