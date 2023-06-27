package ru.skypro.homework.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.skypro.homework.dto.NewPasswordDto;
import ru.skypro.homework.dto.UserDto;
import ru.skypro.homework.model.Avatar;
import ru.skypro.homework.service.UserService;

import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

@Slf4j
@CrossOrigin(value = "http://localhost:3000")
@RestController
@RequestMapping("users")
@Tag(name = "Пользователи")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/set_password")
    @Operation(summary = "Обновление пароля", responses = {
            @ApiResponse(responseCode = "200", content = {@Content(schema = @Schema())}),
            @ApiResponse(responseCode = "401", content = {@Content(schema = @Schema())}),
            @ApiResponse(responseCode = "403", content = {@Content(schema = @Schema())})}
    )
    public ResponseEntity<?> setPassword(@RequestBody NewPasswordDto newPassword) {
        userService.setPassword(newPassword);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/me")
    @Operation(summary = "Обновить информацию об авторизованном пользователю", responses = {
            @ApiResponse(responseCode = "200", content = {@Content(schema = @Schema(
                    implementation = UserDto.class), mediaType = MediaType.APPLICATION_JSON_VALUE)}),
            @ApiResponse(responseCode = "401", content = {@Content(schema = @Schema())})}
    )
    public ResponseEntity<UserDto> updateInfo(@RequestBody UserDto user) {
        return ResponseEntity.ok(userService.update(user));
    }

    @PatchMapping(value = "/me/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Обновить аватар авторизованного пользователя", responses = {
            @ApiResponse(responseCode = "200", content = {@Content(schema = @Schema())}),
            @ApiResponse(responseCode = "401", content = {@Content(schema = @Schema())})}
    )
    public ResponseEntity<byte[]> updateAvatar(@RequestParam("image") MultipartFile avatar) throws IOException {
        return ResponseEntity.ok(userService.updateAvatar(avatar));
    }

    @GetMapping("/me")
    @Operation(summary = "Получить информацию об авторизованном пользователе", responses = {
            @ApiResponse(responseCode = "200", content = {@Content(schema = @Schema(
                    implementation = UserDto.class), mediaType = MediaType.APPLICATION_JSON_VALUE)}),
            @ApiResponse(responseCode = "401", content = {@Content(schema = @Schema())})}
    )
    public ResponseEntity<UserDto> findInfo() {
        return ResponseEntity.ok(userService.findInfo());
    }

    @GetMapping("/me/image")
    @Operation(summary = "Скачать аватар авторизованного пользователя", responses = {
            @ApiResponse(responseCode = "200", content = {@Content(schema = @Schema())})}
    )
    public void downloadAvatar(HttpServletResponse response) throws IOException {
        Avatar avatar = userService.downloadAvatar();
        if (avatar != null) {
            Path path = avatar.getFilePath();
            try (InputStream is = Files.newInputStream(path);
                 OutputStream os = response.getOutputStream();) {
                response.setContentType(avatar.getFileType());
                response.setContentLength((int) avatar.getFileSize());
                is.transferTo(os);
            }
        }
    }

    @GetMapping("/{id}/image")
    @Operation(summary = "Скачать аватар пользователя", responses = {
            @ApiResponse(responseCode = "200", content = {@Content(schema = @Schema())})}
    )
    public void downloadAvatar(@PathVariable("id") Integer id,
                               HttpServletResponse response) throws IOException {
        Avatar avatar = userService.downloadAvatarByUserId(id);
        Path path = avatar.getFilePath();
        try (InputStream is = Files.newInputStream(path);
             OutputStream os = response.getOutputStream();) {
            response.setContentType(avatar.getFileType());
            response.setContentLength((int) avatar.getFileSize());
            is.transferTo(os);
        }
    }
}
