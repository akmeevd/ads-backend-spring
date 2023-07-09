package ru.skypro.homework.controller;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import ru.skypro.homework.dto.UserDto;
import ru.skypro.homework.model.Avatar;
import ru.skypro.homework.service.UserService;
import java.io.*;
import java.nio.file.Path;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@WithMockUser(roles = "USER")
public class UserControllerTest {

    @Autowired
    public WebApplicationContext context;
    private MockMvc mockMvc;
    @Autowired
    private PasswordEncoder encoder;
    @MockBean
    private UserService userService;
    private JSONObject jsonNewPasswordDto;
    private JSONObject jsonUserDto;
    private UserDto userDto;
    private MockMultipartFile file;

    @BeforeEach
    public void setup() throws JSONException, IOException {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();
        jsonNewPasswordDto = new JSONObject();
        jsonNewPasswordDto.put("currentPassword", "1234");
        jsonNewPasswordDto.put("newPassword", "12345");
        jsonUserDto = new JSONObject();
        userDto = new UserDto();
        userDto.setId(1);
        jsonUserDto.put("id", userDto.getId());
        byte[] bytes = {1};
        file = new MockMultipartFile("image", null,
                MediaType.MULTIPART_FORM_DATA_VALUE, bytes);
    }

    @Test
    public void setPassword() throws Exception {
        doNothing().when(userService).setPassword(any());
        mockMvc.perform(post("/users/set_password")
                .content(jsonNewPasswordDto.toString())
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk());
    }

    @Test
    public void updateInfo() throws Exception {
        doReturn(userDto).when(userService).update(any());
        mockMvc.perform(patch("/users/me")
                        .content(jsonUserDto.toString())
                        .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userDto.getId()));
    }

    @Test
    public void findInfo() throws Exception {
        doReturn(userDto).when(userService).findInfo();
        mockMvc.perform(get("/users/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userDto.getId()));
    }

    @Test
    public void downloadAvatar() throws Exception {
        Avatar avatar = new Avatar();
        Path path = Path.of("src\\test\\resources\\picture");
        avatar.setId(1);
        avatar.setPhotoDir(path.toAbsolutePath().toString());
        avatar.setFileExtension("jpeg");
        doReturn(avatar).when(userService).downloadAvatar();
        mockMvc.perform(get("/users/me/image"))
                .andExpect(status().isOk());
    }

    @Test
    public void downloadAvatar_2() throws Exception {
        Avatar avatar = new Avatar();
        Path path = Path.of("src\\test\\resources\\picture");
        avatar.setId(1);
        avatar.setPhotoDir(path.toAbsolutePath().toString());
        avatar.setFileExtension("jpeg");
        doReturn(avatar).when(userService).downloadAvatarByUserId(anyInt());
        mockMvc.perform(get("/users/" + userDto.getId() + "/image"))
                .andExpect(status().isOk());
    }

    @Test
    public void updateAvatar() throws Exception {
        byte[] bytes = {1};
        doReturn(bytes).when(userService).updateAvatar(any());
        mockMvc.perform(multipart(HttpMethod.PATCH, "/users/me/image")
                        .file(file))
                .andExpect(status().isOk());
    }
}
