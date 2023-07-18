package ru.skypro.homework.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import ru.skypro.homework.exception.ImageUploadException;
import ru.skypro.homework.model.*;
import ru.skypro.homework.repository.ImageRepository;
import ru.skypro.homework.repository.UserRepository;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Files;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class ImageServiceTest {
    @InjectMocks
    private ImageService imageService;
    @Mock
    private ImageRepository imageRepository;
    @Mock
    private UserRepository userRepository;
    private MockMultipartFile mockMultipartFile;
    private Image photo, avatar;

    @BeforeEach
    public void setup() throws IOException {
        String dir = "src/test/resources/picture/test";
        Resource resource = new ClassPathResource("picture/images.jpeg");
        mockMultipartFile = new MockMultipartFile(
                "image",
                "image.jpeg",
                MediaType.IMAGE_JPEG_VALUE,
                Files.readAllBytes(resource.getFile().toPath())
        );

        photo = new Photo(dir);
        photo.setId(1);
        avatar = new Avatar(dir);
        avatar.setId(2);
    }

    @Test
    public void uploadImage() throws NoSuchMethodException,
            InvocationTargetException, IllegalAccessException {
        getPrivateMethods(photo);
        doReturn(photo).when(imageRepository).save(any());
        Photo photo = imageService.uploadPhoto(mockMultipartFile);
        assertNotNull(photo);
        assertEquals(photo.getFileSize(), mockMultipartFile.getSize());
        verify(imageRepository, times(1)).save(any());
    }

    @Test
    public void uploadImage_2() throws InvocationTargetException,
            NoSuchMethodException, IllegalAccessException {
        getPrivateMethods(photo);
        Advert advert = new Advert();
        advert.setPhoto((Photo) photo);
        doReturn(photo).when(imageRepository).save(any());
        Photo photo = imageService.uploadPhoto(advert, mockMultipartFile);
        assertNotNull(photo);
        assertEquals(photo.getFileSize(), mockMultipartFile.getSize());
        verify(imageRepository, times(1)).save(any());
    }

    @Test
    public void uploadAvatar() throws InvocationTargetException,
            NoSuchMethodException, IllegalAccessException {
        getPrivateMethods(avatar);
        User user = new User();
        user.setAvatar((Avatar) avatar);
        doReturn(avatar).when(imageRepository).save(any());
        doReturn(user).when(userRepository).save(any());
        Avatar avatar = imageService.uploadAvatar(user, mockMultipartFile);
        assertNotNull(avatar);
        assertEquals(avatar, avatar);
        verify(imageRepository, times(1)).save(any());
        verify(userRepository, times(1)).save(any());
    }

    private void getPrivateMethods(Image image) throws NoSuchMethodException,
            InvocationTargetException, IllegalAccessException {
        Class[] parameters1 = new Class[2];
        parameters1[0] = MultipartFile.class;
        parameters1[1] = Image.class;
        Method method1 = imageService.getClass().getDeclaredMethod("mapFileToImage", parameters1);
        method1.setAccessible(true);
        Object[] arguments1 = new Object[2];
        arguments1[0] = mockMultipartFile;
        arguments1[1] = image;
        method1.invoke(imageService, arguments1);
        Class[] parameters2 = new Class[2];
        parameters2[0] = Image.class;
        parameters2[1] = MultipartFile.class;
        Method method2 = imageService.getClass().getDeclaredMethod("upload", parameters2);
        method2.setAccessible(true);
        Object[] arguments2 = new Object[2];
        arguments2[0] = image;
        arguments2[1] = mockMultipartFile;
        method2.invoke(imageService, arguments2);
    }

    @Test
    public void doesThrowPhotoUploadExceptionWhenUploadAvatar() {
        User user = new User();
        avatar.setImageDir(null);
        doReturn(avatar).when(imageRepository).save(any());
        assertThrows(ImageUploadException.class,
                () -> imageService.uploadAvatar(user, mockMultipartFile));
    }

    @Test
    public void doesThrowPhotoUploadExceptionWhenUploadImage() {
        Advert advert = new Advert();
        photo.setImageDir(null);
        doReturn(photo).when(imageRepository).save(any());
        assertThrows(ImageUploadException.class,
                () -> imageService.uploadPhoto(mockMultipartFile));
        assertThrows(ImageUploadException.class,
                () -> imageService.uploadPhoto(advert, mockMultipartFile));
    }
}
