package ru.skypro.homework.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import ru.skypro.homework.exception.PhotoUploadException;
import ru.skypro.homework.model.*;
import ru.skypro.homework.repository.PhotoRepository;
import ru.skypro.homework.repository.UserRepository;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class PhotoServiceTest {
    @InjectMocks
    private PhotoService photoService;
    @Mock
    private PhotoRepository photoRepository;
    @Mock
    private UserRepository userRepository;
    private MockMultipartFile mockMultipartFile;
    private Photo image, avatar;

    @BeforeEach
    public void setup() throws IOException {
        String dir = "src\\test\\resources\\picture";
        File file = new File(dir + "\\images.jpeg");
        try {
            InputStream is = new FileInputStream(file);
            byte[] bytes = is.readAllBytes();
            is.close();
            mockMultipartFile = new MockMultipartFile("picture",
                    "images.jpeg", "image/jpeg", bytes);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        image = new Image(dir);
        image.setId(1);
        avatar = new Avatar(dir);
        avatar.setId(2);
    }

    @Test
    public void uploadImage() throws NoSuchMethodException,
            InvocationTargetException, IllegalAccessException {
        getPrivateMethods(image);
        doReturn(image).when(photoRepository).save(any());
        Image image = photoService.uploadImage(mockMultipartFile);
        assertNotNull(image);
        assertEquals(image.getFileSize(), mockMultipartFile.getSize());
        verify(photoRepository, times(1)).save(any());
    }

    @Test
    public void uploadImage_2() throws InvocationTargetException,
            NoSuchMethodException, IllegalAccessException {
        getPrivateMethods(image);
        Advert advert = new Advert();
        advert.setImage((Image) image);
        doReturn(image).when(photoRepository).save(any());
        Image image = photoService.uploadImage(advert, mockMultipartFile);
        assertNotNull(image);
        assertEquals(image.getFileSize(), mockMultipartFile.getSize());
        verify(photoRepository, times(1)).save(any());
    }

    @Test
    public void uploadAvatar() throws InvocationTargetException,
            NoSuchMethodException, IllegalAccessException {
        getPrivateMethods(avatar);
        User user = new User();
        user.setAvatar((Avatar) avatar);
        doReturn(avatar).when(photoRepository).save(any());
        doReturn(user).when(userRepository).save(any());
        Avatar avatar = photoService.uploadAvatar(user, mockMultipartFile);
        assertNotNull(avatar);
        assertEquals(avatar, avatar);
        verify(photoRepository, times(1)).save(any());
        verify(userRepository, times(1)).save(any());
    }

    public void getPrivateMethods(Photo photo) throws NoSuchMethodException,
            InvocationTargetException, IllegalAccessException {
        Class[] parameters1 = new Class[2];
        parameters1[0] = MultipartFile.class;
        parameters1[1] = Photo.class;
        Method method1 = photoService.getClass().getDeclaredMethod("mapFileToPhoto", parameters1);
        method1.setAccessible(true);
        Object[] arguments1 = new Object[2];
        arguments1[0] = mockMultipartFile;
        arguments1[1] = photo;
        method1.invoke(photoService, arguments1);
        Class[] parameters2 = new Class[2];
        parameters2[0] = Photo.class;
        parameters2[1] = MultipartFile.class;
        Method method2 = photoService.getClass().getDeclaredMethod("upload", parameters2);
        method2.setAccessible(true);
        Object[] arguments2 = new Object[2];
        arguments2[0] = photo;
        arguments2[1] = mockMultipartFile;
        method2.invoke(photoService, arguments2);
    }

    @Test
    public void doesThrowPhotoUploadExceptionWhenUploadAvatar() {
        User user = new User();
        avatar.setPhotoDir(null);
        doReturn(avatar).when(photoRepository).save(any());
        assertThrows(PhotoUploadException.class,
                () -> photoService.uploadAvatar(user, mockMultipartFile));
    }

    @Test
    public void doesThrowPhotoUploadExceptionWhenUploadImage() {
        Advert advert = new Advert();
        image.setPhotoDir(null);
        doReturn(image).when(photoRepository).save(any());
        assertThrows(PhotoUploadException.class,
                () -> photoService.uploadImage(mockMultipartFile));
        assertThrows(PhotoUploadException.class,
                () -> photoService.uploadImage(advert, mockMultipartFile));
    }
}
