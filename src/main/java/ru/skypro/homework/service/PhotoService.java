package ru.skypro.homework.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import ru.skypro.homework.exception.PhotoUploadException;
import ru.skypro.homework.model.*;
import ru.skypro.homework.repository.PhotoRepository;
import ru.skypro.homework.repository.UserRepository;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

@Service
@Slf4j
public class PhotoService {
    @Value("${path.to.images.folder}")
    private String imagesDir;
    @Value("${path.to.avatars.folder}")
    private String avatarsDir;

    private final PhotoRepository photoRepository;
    private final UserRepository userRepository;

    public PhotoService(PhotoRepository photoRepository, UserRepository userRepository) {
        this.photoRepository = photoRepository;
        this.userRepository = userRepository;
    }

    /**
     * Upload new advert image
     *
     * @param file image file
     * @return photo object
     */
    @Transactional
    public Image uploadImage(MultipartFile file) {
        log.info("upload new advert image");
        try {
            Image image = new Image(imagesDir);
            mapFileToPhoto(file, image);
            image = photoRepository.save(image);
            upload(image, file);
            return image;
        } catch (Exception e) {
            throw new PhotoUploadException(e.getMessage());
        }
    }

    /**
     * Update advert image
     *
     * @param advert advert object
     * @param file   image file
     * @return photo object
     */
    @Transactional
    public Image uploadImage(Advert advert, MultipartFile file) {
        log.info("upload advert image");
        try {
            Image image = advert.getImage();
            mapFileToPhoto(file, image);
            image = photoRepository.save(image);
            upload(image, file);
            return image;
        } catch (Exception e) {
            throw new PhotoUploadException(e.getMessage());
        }
    }

    /**
     * Update user avatar
     *
     * @param file avatar file
     * @return photo object
     */
    @Transactional
    public Photo uploadAvatar(User user, MultipartFile file) {
        log.info("upload user avatar");
        try {
            Avatar avatar = new Avatar(avatarsDir);
            mapFileToPhoto(file, avatar);
            photoRepository.save(avatar);
            upload(avatar, file);
            user.setAvatar(avatar);
            userRepository.save(user);
            return avatar;
        } catch (Exception e) {
            throw new PhotoUploadException(e.getMessage());
        }
    }

    private void upload(Photo photo, MultipartFile file) throws IOException {
        Path filePath = photo.getFilePath();
        Files.createDirectories(filePath.getParent());
        Files.deleteIfExists(filePath);
        Files.write(filePath, file.getBytes());
    }

    private void mapFileToPhoto(MultipartFile file, Photo photo) {
        photo.setFileType(file.getContentType());
        photo.setFileName(file.getOriginalFilename());
        photo.setFileExtension(StringUtils.getFilenameExtension(file.getOriginalFilename()));
        photo.setFileSize(file.getSize());
    }
}
