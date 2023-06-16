package ru.skypro.homework.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import ru.skypro.homework.dto.UserDto;
import ru.skypro.homework.model.Avatar;
import ru.skypro.homework.model.Image;
import ru.skypro.homework.model.User;

@Mapper(componentModel = "spring")
public interface UserMapper {
    static String avatarToUrl(Avatar avatar) {
        if (avatar == null) {
            return "";
        }
        return "/users/me/image";
    }

    User userDtoToUser(UserDto userDto);

    @Mapping(target = "image", source = "avatar")
    UserDto userToUserDto(User user);

    @Mapping(target = "email", ignore = true)
    @Mapping(target = "password", ignore = true)
    void updateUser(UserDto userDto, @MappingTarget User user);
}
