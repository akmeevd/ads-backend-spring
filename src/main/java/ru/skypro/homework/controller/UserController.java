package ru.skypro.homework.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.skypro.homework.dto.NewPasswordDto;
import ru.skypro.homework.dto.UserDto;
import ru.skypro.homework.exception.ImageDownloadException;
import ru.skypro.homework.model.Avatar;
import ru.skypro.homework.model.Image;
import ru.skypro.homework.service.UserService;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;

@Slf4j
@CrossOrigin(value = "http://localhost:3000")
@RestController
@RequestMapping("users")
@Tag(name = "Users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/set_password")
    @Operation(summary = "Update password", responses = {
            @ApiResponse(responseCode = "200", content = {@Content(schema = @Schema())}),
            @ApiResponse(responseCode = "401", content = {@Content(schema = @Schema())}),
            @ApiResponse(responseCode = "403", content = {@Content(schema = @Schema())})}
    )
    public ResponseEntity<?> setPassword(@RequestBody NewPasswordDto newPassword) {
        userService.setPassword(newPassword);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/me")
    @Operation(summary = "Update data of authorized user", responses = {
            @ApiResponse(responseCode = "200", content = {@Content(schema = @Schema(
                    implementation = UserDto.class), mediaType = MediaType.APPLICATION_JSON_VALUE)}),
            @ApiResponse(responseCode = "401", content = {@Content(schema = @Schema())})}
    )
    public ResponseEntity<UserDto> updateInfo(@RequestBody UserDto user) {
        return ResponseEntity.ok(userService.update(user));
    }

    @PatchMapping(value = "/me/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Update avatar of authorized user", responses = {
            @ApiResponse(responseCode = "200", content = {@Content(schema = @Schema())}),
            @ApiResponse(responseCode = "401", content = {@Content(schema = @Schema())})}
    )
    public ResponseEntity<byte[]> updateAvatar(@RequestParam("image") MultipartFile avatar) {
        return ResponseEntity.ok(userService.updateImage(avatar));
    }

    @GetMapping("/me")
    @Operation(summary = "Get info of authorized user", responses = {
            @ApiResponse(responseCode = "200", content = {@Content(schema = @Schema(
                    implementation = UserDto.class), mediaType = MediaType.APPLICATION_JSON_VALUE)}),
            @ApiResponse(responseCode = "401", content = {@Content(schema = @Schema())})}
    )
    public ResponseEntity<UserDto> findInfo() {
        return ResponseEntity.ok(userService.findInfo());
    }

    @GetMapping("/me/image")
    @Operation(summary = "Get avatar of authorized user", responses = {
            @ApiResponse(responseCode = "200", content = {@Content(schema = @Schema())})}
    )
    public void downloadImage(HttpServletResponse response) {
        Image avatar = userService.downloadImage();
        downloadAvatar(response, avatar);
    }

    @GetMapping("/{id}/image")
    @Operation(summary = "Get avatar of user by id", responses = {
            @ApiResponse(responseCode = "200", content = {@Content(schema = @Schema())})}
    )
    public void downloadImage(@PathVariable("id") Integer id,
                              HttpServletResponse response) {
        Image avatar = userService.downloadImageByUserId(id);
        downloadAvatar(response, avatar);
    }

    private void downloadAvatar(HttpServletResponse response, Image avatar) {
        if (avatar != null) {
            try (InputStream is = Files.newInputStream(avatar.getFilePath());
                 OutputStream os = response.getOutputStream();) {
                response.setContentType(avatar.getFileType());
                response.setContentLength((int) avatar.getFileSize());
                is.transferTo(os);
            } catch (IOException exception) {
                throw new ImageDownloadException(exception.getMessage());
            }
        }
    }
}
