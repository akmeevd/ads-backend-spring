package ru.skypro.homework.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.skypro.homework.dto.*;
import ru.skypro.homework.exception.ImageDownloadException;
import ru.skypro.homework.model.Image;
import ru.skypro.homework.model.Photo;
import ru.skypro.homework.service.AdvertService;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

@Slf4j
@CrossOrigin(value = "http://localhost:3000")
@RestController
@RequestMapping("ads")
@Tag(name = "Adverts")
public class AdvertController {
    private final AdvertService advertService;

    public AdvertController(AdvertService advertService) {
        this.advertService = advertService;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Create new advert", responses = {
            @ApiResponse(responseCode = "201", content = {@Content(schema = @Schema(
                    implementation = AdsDto.class), mediaType = MediaType.APPLICATION_JSON_VALUE)}),
            @ApiResponse(responseCode = "401", content = {@Content(schema = @Schema())})}
    )
    public ResponseEntity<AdsDto> create(@RequestPart CreateAdsDto properties,
                                         @RequestPart(name = "image") MultipartFile file) {
        return new ResponseEntity<>(advertService.create(properties, file), HttpStatus.CREATED);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete advert", responses = {
            @ApiResponse(responseCode = "204", content = {@Content(schema = @Schema())}),
            @ApiResponse(responseCode = "401", content = {@Content(schema = @Schema())}),
            @ApiResponse(responseCode = "403", content = {@Content(schema = @Schema())})}
    )
    public ResponseEntity<?> delete(@PathVariable("id") Integer id) {
        advertService.delete(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Update advert", responses = {
            @ApiResponse(responseCode = "200", content = {@Content(schema = @Schema(
                    implementation = AdsDto.class), mediaType = MediaType.APPLICATION_JSON_VALUE)}),
            @ApiResponse(responseCode = "401", content = {@Content(schema = @Schema())}),
            @ApiResponse(responseCode = "403", content = {@Content(schema = @Schema())})}
    )
    public ResponseEntity<AdsDto> update(@PathVariable("id") Integer id,
                                         @RequestBody CreateAdsDto advert) {
        return ResponseEntity.ok(advertService.update(id, advert));
    }

    @PatchMapping(value = "/{id}/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Update advert image", responses = {
            @ApiResponse(responseCode = "200", content = {@Content(schema = @Schema(
                    implementation = byte[].class), mediaType = MediaType.APPLICATION_OCTET_STREAM_VALUE)}),
            @ApiResponse(responseCode = "401", content = {@Content(schema = @Schema())}),
            @ApiResponse(responseCode = "403", content = {@Content(schema = @Schema())})}
    )
    public ResponseEntity<byte[]> updateImage(@PathVariable("id") Integer id,
                                              @RequestParam("image") MultipartFile file) {
        return ResponseEntity.ok(advertService.updateImage(id, file));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get advert details", responses = {
            @ApiResponse(responseCode = "200", content = {@Content(schema = @Schema(
                    implementation = FullAdsDto.class), mediaType = MediaType.APPLICATION_JSON_VALUE)}),
            @ApiResponse(responseCode = "401", content = {@Content(schema = @Schema())})}
    )
    public ResponseEntity<FullAdsDto> findById(@PathVariable("id") Integer id) {
        return ResponseEntity.ok(advertService.findById(id));
    }

    @GetMapping("/me")
    @Operation(summary = "Get adverts for authorized user", responses = {
            @ApiResponse(responseCode = "200", content = {@Content(schema = @Schema(
                    implementation = ResponseWrapperAdsDto.class), mediaType = MediaType.APPLICATION_JSON_VALUE)}),
            @ApiResponse(responseCode = "401", content = {@Content(schema = @Schema())})}
    )
    public ResponseEntity<ResponseWrapperAdsDto> findAllByAuthUser() {
        return ResponseEntity.ok(advertService.findAllByAuthUser());
    }

    @GetMapping
    @Operation(summary = "Get all adverts", responses = {
            @ApiResponse(responseCode = "200", content = {@Content(schema = @Schema(
                    implementation = ResponseWrapperAdsDto.class), mediaType = MediaType.APPLICATION_JSON_VALUE)})}
    )
    public ResponseEntity<ResponseWrapperAdsDto> findAll() {
        return ResponseEntity.ok(advertService.findAll());
    }

    @GetMapping("/{id}/image")
    @Operation(summary = "Download advert image", responses = {
            @ApiResponse(responseCode = "200", content = {@Content(schema = @Schema())})}
    )
    public void downloadImage(@PathVariable("id") Integer id,
                              HttpServletResponse response) {
        Image photo = advertService.downloadImage(id);
        Path path = photo.getFilePath();
        try (InputStream is = Files.newInputStream(path);
             OutputStream os = response.getOutputStream();) {
            response.setContentType(photo.getFileType());
            response.setContentLength((int) photo.getFileSize());
            is.transferTo(os);
        } catch (IOException exception) {
            throw new ImageDownloadException(exception.getMessage());
        }
    }
}
