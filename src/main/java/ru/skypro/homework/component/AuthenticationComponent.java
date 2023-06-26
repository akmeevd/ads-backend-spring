package ru.skypro.homework.component;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import ru.skypro.homework.model.Role;

@Component
public class AuthenticationComponent implements IAuthenticationFacade {
    @Override
    public Authentication getAuth() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    public boolean check(String username) {
        if (getAuth() == null) {
            return true;
        } else if (checkAdminRole()) {
            return false;
        } else {
            return !checkUsername(username);
        }
    }

    public boolean checkAdminRole() {
        return getAuth().getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals(Role.ADMIN.getAuthority()));
    }

    public boolean checkUsername(String username) {
        return getAuth().getName().equals(username);
    }
}
