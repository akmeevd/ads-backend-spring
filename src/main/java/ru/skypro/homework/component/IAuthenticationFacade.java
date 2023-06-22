package ru.skypro.homework.component;

import org.springframework.security.core.Authentication;

public interface IAuthenticationFacade {
    Authentication getAuth();
}
