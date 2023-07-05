package ru.skypro.homework.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import ru.skypro.homework.dto.RegisterReqDto;
import ru.skypro.homework.dto.UserDto;
import ru.skypro.homework.model.User;

@Mapper(componentModel = "spring")
public interface UserMapper {
    User userDtoToUser(UserDto userDto);

    @Mapping(target = "image", expression = "java(getUrlToAvatar(user))")
    UserDto userToUserDto(User user);

    @Mapping(target = "username", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "id", expression = "java(user.getId())")
    void updateUser(UserDto userDto, @MappingTarget User user);

    @Mapping(target = "username", ignore = true)
    @Mapping(target = "password", ignore = true)
    void updateUser(RegisterReqDto registerReqDto, @MappingTarget User user);

    default String getUrlToAvatar(User user) {
        if (user.getAvatar() == null){
            return null;
        }
        return "/users/me/image";
    }
}
