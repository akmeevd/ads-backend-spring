package ru.skypro.homework.securing_dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.skypro.homework.model.Role;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SecuringUserDto {

    private String username;
    private String password;
    private Role role;
    private boolean enabled;
}
