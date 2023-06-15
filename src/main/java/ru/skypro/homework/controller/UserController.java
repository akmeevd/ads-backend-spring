package ru.skypro.homework.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.skypro.homework.dto.NewPasswordDto;
import ru.skypro.homework.dto.UserDto;
import ru.skypro.homework.mapper.UserMapper;
import ru.skypro.homework.model.Avatar;
import ru.skypro.homework.model.Image;
import ru.skypro.homework.model.User;
import ru.skypro.homework.service.UserService;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

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
    public ResponseEntity<?> setPassword(Authentication auth, @RequestBody NewPasswordDto newPassword) {
        userService.setPassword(auth, newPassword);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/me")
    @Operation(summary = "Обновить информацию об авторизованном пользователю", responses = {
            @ApiResponse(responseCode = "200", content = {@Content(schema = @Schema(
                    implementation = UserDto.class), mediaType = MediaType.APPLICATION_JSON_VALUE)}),
            @ApiResponse(responseCode = "401", content = {@Content(schema = @Schema())})}
    )
    public ResponseEntity<UserDto> updateInfo(Authentication auth, @RequestBody UserDto user) {
        return ResponseEntity.ok(userService.updateInfo(auth, user));
    }

    @PatchMapping(value = "/me/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Обновить аватар авторизованного пользователя", responses = {
            @ApiResponse(responseCode = "200", content = {@Content(schema = @Schema())}),
            @ApiResponse(responseCode = "401", content = {@Content(schema = @Schema())})}
    )
    public ResponseEntity<byte[]> updateImage(Authentication auth, @RequestParam("image") MultipartFile avatar) throws IOException {
        return ResponseEntity.ok(userService.updateImage(auth, avatar));
    }

    @GetMapping("/me/image")
    public void downloadImage(Authentication authentication,
                              HttpServletResponse response) throws IOException {
        Avatar avatar = userService.downloadImage(authentication);
        Path path = avatar.getFilePath();
        try (InputStream is = Files.newInputStream(path);
             OutputStream os = response.getOutputStream();) {
            response.setContentType(avatar.getFileType());
            response.setContentLength((int) avatar.getFileSize());
            is.transferTo(os);
        }
    }

        @GetMapping("/me")
        @Operation(summary = "Получить информацию об авторизованном пользователе", responses = {
                @ApiResponse(responseCode = "200", content = {@Content(schema = @Schema(
                        implementation = UserDto.class), mediaType = MediaType.APPLICATION_JSON_VALUE)}),
                @ApiResponse(responseCode = "401", content = {@Content(schema = @Schema())})}
        )
        public ResponseEntity<UserDto> findInfo (Authentication auth){

            return ResponseEntity.ok(userService.findInfo(auth));
        }
    }
