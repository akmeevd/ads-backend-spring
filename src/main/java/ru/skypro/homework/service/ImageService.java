package ru.skypro.homework.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import ru.skypro.homework.exception.ImageUploadException;
import ru.skypro.homework.model.*;
import ru.skypro.homework.repository.CommentRepository;
import ru.skypro.homework.repository.ImageRepository;
import ru.skypro.homework.repository.UserRepository;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Service for maintain images via {@link ImageRepository}
 */
@Service
@Slf4j
public class ImageService {
    @Value("${path.to.photos.folder}")
    private String photosDir;
    @Value("${path.to.avatars.folder}")
    private String avatarsDir;

    private final ImageRepository imageRepository;
    private final UserRepository userRepository;

    public ImageService(ImageRepository imageRepository, UserRepository userRepository) {
        this.imageRepository = imageRepository;
        this.userRepository = userRepository;
    }

    /**
     * Upload new advert photo
     *
     * @param file {@link MultipartFile}
     * @return {@link Photo}
     */
    @Transactional
    public Photo uploadPhoto(MultipartFile file) {
        log.info("Upload new advert photo");
        try {
            Photo photo = new Photo(photosDir);
            mapFileToImage(file, photo);
            photo = imageRepository.save(photo);
            upload(photo, file);
            return photo;
        } catch (Exception exception) {
            log.error(exception.getMessage());
            throw new ImageUploadException(exception.getMessage());
        }
    }

    /**
     * Update advert photo
     *
     * @param advert {@link Advert}
     * @param file   {@link MultipartFile}
     * @return {@link Photo}
     */
    @Transactional
    public Photo uploadPhoto(Advert advert, MultipartFile file) {
        log.info("Upload advert photo");
        try {
            Photo photo = advert.getPhoto();
            mapFileToImage(file, photo);
            photo = imageRepository.save(photo);
            upload(photo, file);
            return photo;
        } catch (Exception exception) {
            log.error(exception.getMessage());
            throw new ImageUploadException(exception.getMessage());
        }
    }

    /**
     * Update user avatar
     *
     * @param user {@link User}
     * @param file {@link MultipartFile}
     * @return {@link Avatar}
     */
    @Transactional
    public Avatar uploadAvatar(User user, MultipartFile file) {
        log.info("Upload user avatar");
        try {
            Avatar avatar = user.getAvatar();
            if (avatar == null) {
                avatar = new Avatar(avatarsDir);
            }
            mapFileToImage(file, avatar);
            avatar = imageRepository.save(avatar);
            upload(avatar, file);
            user.setAvatar(avatar);
            userRepository.save(user);
            return avatar;
        } catch (Exception exception) {
            log.error(exception.getMessage());
            throw new ImageUploadException(exception.getMessage());
        }
    }

    /**
     * Delete image from file system
     *
     * @param image {@link Image}
     */
    @Transactional
    public void deleteFile(Image image) {
        if (image != null) {
            try {
                log.info("Delete image id " + image.getId());
                Files.deleteIfExists(image.getFilePath().toAbsolutePath().toFile().toPath());
            } catch (IOException exception) {
                log.error(exception.getMessage());
                throw new ImageUploadException(exception.getMessage());
            }
        }
    }

    private void upload(Image image, MultipartFile file) throws IOException {
        Path filePath = image.getFilePath();
        Files.createDirectories(filePath.getParent());
        Files.deleteIfExists(filePath);
        Files.write(filePath, file.getBytes());
    }

    private void mapFileToImage(MultipartFile file, Image image) {
        image.setFileType(file.getContentType());
        image.setFileName(file.getOriginalFilename());
        image.setFileExtension(StringUtils.getFilenameExtension(file.getOriginalFilename()));
        image.setFileSize(file.getSize());
    }
}
