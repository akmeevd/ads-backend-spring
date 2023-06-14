package ru.skypro.homework.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import ru.skypro.homework.exception.PhotoUploadException;
import ru.skypro.homework.model.Advert;
import ru.skypro.homework.model.Image;
import ru.skypro.homework.model.Photo;
import ru.skypro.homework.model.User;
import ru.skypro.homework.repository.PhotoRepository;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

import static java.nio.file.StandardOpenOption.CREATE_NEW;

@Service
@Slf4j
public class PhotoService {
    @Value("${path.to.images.folder}")
    private String imagesDir;
    @Value("${path.to.avatars.folder}")
    private String avatarsDir;

    private final PhotoRepository photoRepository;

    public PhotoService(PhotoRepository photoRepository) {
        this.photoRepository = photoRepository;
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
            mapFileToImage(file, image);
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
            mapFileToImage(file, image);
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
     * @param user user object
     * @param file avatar file
     * @return photo object
     */
    @Transactional
    public Photo uploadAvatar(User user, MultipartFile file) {
        log.info("upload user avatar");
        //to be done
        return null;
    }

    private void upload(Photo photo, MultipartFile file) throws IOException {
        Path filePath = photo.getFilePath();
        Files.createDirectories(filePath.getParent());
        Files.deleteIfExists(filePath);
        try (InputStream is = file.getInputStream();
             OutputStream os = Files.newOutputStream(filePath, CREATE_NEW);
             BufferedInputStream bis = new BufferedInputStream(is, 1024);
             BufferedOutputStream bos = new BufferedOutputStream(os, 1024);
        ) {
            bis.transferTo(bos);
        }
    }

    private String getFileExtensions(String fileName) {
        return fileName.substring(fileName.lastIndexOf(".") + 1);
    }

    private void mapFileToImage(MultipartFile file, Image image) {
        image.setFileType(file.getContentType());
        image.setFileName(file.getOriginalFilename());
        image.setFileExtension(getFileExtensions(Objects.requireNonNull(file.getOriginalFilename())));
        image.setFileSize(file.getSize());
    }
}
