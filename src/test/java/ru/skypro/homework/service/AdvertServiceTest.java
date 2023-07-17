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
import org.springframework.security.core.Authentication;
import ru.skypro.homework.component.AuthenticationComponent;
import ru.skypro.homework.dto.AdsDto;
import ru.skypro.homework.dto.CreateAdsDto;
import ru.skypro.homework.dto.FullAdsDto;
import ru.skypro.homework.dto.ResponseWrapperAdsDto;
import ru.skypro.homework.exception.ActionForbiddenException;
import ru.skypro.homework.exception.AdvertNotFoundException;
import ru.skypro.homework.exception.UserUnauthorizedException;
import ru.skypro.homework.mapper.AdvertMapper;
import ru.skypro.homework.model.*;
import ru.skypro.homework.repository.AdvertRepository;
import ru.skypro.homework.repository.UserRepository;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class AdvertServiceTest {

    @InjectMocks
    private AdvertService advertService;
    @Mock
    private AdvertRepository advertRepository;
    @Mock
    private AdvertMapper advertMapper;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ImageService imageService;
    @Mock
    private AuthenticationComponent auth;
    @Mock
    private Authentication authentication;
    private MockMultipartFile mockMultipartFile;
    private Advert advert;


    @BeforeEach
    public void setup() throws IOException {
        User user = new User();
        user.setId(1);
        user.setRole(Role.ADMIN);
        advert = new Advert();
        advert.setId(1);
        advert.setAuthor(user);
        advert.setPhoto(new Photo());
        Resource resource = new ClassPathResource("picture/images.jpeg");
        mockMultipartFile = new MockMultipartFile(
                "image",
                "image.jpeg",
                MediaType.IMAGE_JPEG_VALUE,
                Files.readAllBytes(resource.getFile().toPath())
        );
    }

    @Test
    public void create() {
        CreateAdsDto properties = new CreateAdsDto();
        AdsDto expectedAdsDto = new AdsDto();
        doReturn(advert).when(advertMapper).createAdsDtoToAdvert(any());
        doReturn(expectedAdsDto).when(advertMapper).advertToAdsDto(any());
        AdsDto actualAdsDto = advertService.create(properties, mockMultipartFile);
        assertNotNull(actualAdsDto);
        assertEquals(expectedAdsDto, actualAdsDto);
    }

    @Test
    public void update() {
        CreateAdsDto createAdsDto = new CreateAdsDto();
        AdsDto expectedAdsDto = new AdsDto();
        doReturn(Optional.of(advert)).when(advertRepository).findById(any());
        doNothing().when(advertMapper).updateAdvert(isA(CreateAdsDto.class), isA(Advert.class));
        doReturn(advert).when(advertRepository).save(any());
        doReturn(expectedAdsDto).when(advertMapper).advertToAdsDto(advert);
        AdsDto actualAdsDto = advertService.update(advert.getId(), createAdsDto);
        assertNotNull(actualAdsDto);
        assertEquals(expectedAdsDto, actualAdsDto);
    }

    @Test
    public void delete() {
        doReturn(Optional.of(advert)).when(advertRepository).findById(any());
        advertService.delete(advert.getId());
        verify(advertRepository, times(1)).delete(any());
    }

    @Test
    public void updateImage() throws IOException {
        doReturn(Optional.of(advert)).when(advertRepository).findById(any());
        doReturn(advert.getPhoto()).when(imageService).uploadPhoto(any(), any());
        byte[] actualImageBytes = advertService.updateImage(advert.getId(), mockMultipartFile);
        assertNotNull(actualImageBytes);
        assertArrayEquals(mockMultipartFile.getBytes(), actualImageBytes);
    }

    @Test
    public void downloadImage() {
        doReturn(Optional.of(advert)).when(advertRepository).findById(any());
        Image actualPhoto = advertService.downloadImage(advert.getId());
        assertNotNull(actualPhoto);
        assertEquals(advert.getPhoto(), actualPhoto);
    }

    @Test
    public void findAll() {
        AdsDto adsDto = new AdsDto();
        adsDto.setPk(advert.getId());
        ResponseWrapperAdsDto expectedResponseWrapperAdsDto = new ResponseWrapperAdsDto();
        expectedResponseWrapperAdsDto.setCount(1);
        expectedResponseWrapperAdsDto.setResults(List.of(adsDto));
        doReturn(List.of(advert)).when(advertRepository).findAll();
        doReturn(expectedResponseWrapperAdsDto).when(advertMapper).listToRespWrapperAdsDto(any());
        ResponseWrapperAdsDto actualResponseWrapperAdsDto = advertService.findAll();
        assertEquals(expectedResponseWrapperAdsDto, actualResponseWrapperAdsDto);
    }

    @Test
    public void findById() {
        FullAdsDto expectedFullAdsDto = new FullAdsDto();
        expectedFullAdsDto.setPk(advert.getId());
        doReturn(Optional.of(advert)).when(advertRepository).findById(any());
        doReturn(expectedFullAdsDto).when(advertMapper).advertToFullAdsDto(any());
        FullAdsDto actualFullAdsDto = advertService.findById(advert.getId());
        assertNotNull(actualFullAdsDto);
        assertEquals(expectedFullAdsDto, actualFullAdsDto);
    }

    @Test
    public void findAllByAuthUser() {
        User user = advert.getAuthor();
        ResponseWrapperAdsDto expectedResponseWrapperAdsDto = new ResponseWrapperAdsDto();
        AdsDto adsDto = new AdsDto();
        adsDto.setPk(advert.getId());
        expectedResponseWrapperAdsDto.setCount(1);
        expectedResponseWrapperAdsDto.setResults(List.of(adsDto));
        doReturn(user).when(userRepository).findByUsername(any());
        doReturn(authentication).when(auth).getAuth();
        doReturn(List.of(advert)).when(advertRepository).findByAuthorId(anyInt());
        doReturn(expectedResponseWrapperAdsDto).when(advertMapper).listToRespWrapperAdsDto(List.of(advert));
        ResponseWrapperAdsDto actualResponseWrapperAdsDto = advertService.findAllByAuthUser();
        assertNotNull(actualResponseWrapperAdsDto);
        assertEquals(expectedResponseWrapperAdsDto, actualResponseWrapperAdsDto);
    }

    @Test
    public void findAdvert() throws NoSuchMethodException,
            InvocationTargetException, IllegalAccessException {
        doReturn(Optional.of(advert)).when(advertRepository).findById(anyInt());
        Class[] parameters = new Class[1];
        parameters[0] = int.class;
        Method method = advertService
                .getClass()
                .getDeclaredMethod("findAdvert", parameters);
        method.setAccessible(true);
        Object[] methodArguments = new Object[1];
        methodArguments[0] = advert.getId();
        Advert actualAdvert = (Advert) method.invoke(advertService, methodArguments);
        assertNotNull(actualAdvert);
        assertEquals(advert, actualAdvert);
    }

    @Test
    public void findAdvertWithAuth() throws NoSuchMethodException,
            InvocationTargetException, IllegalAccessException {
        doReturn(Optional.of(advert)).when(advertRepository).findById(any());
        Class[] parameters = new Class[1];
        parameters[0] = int.class;
        Method method = advertService
                .getClass()
                .getDeclaredMethod("findAdvertWithAuth", parameters);
        method.setAccessible(true);
        Object[] arguments = new Object[1];
        arguments[0] = advert.getId();
        Advert actualAdvert = (Advert) method.invoke(advertService, arguments);
        assertNotNull(actualAdvert);
        assertEquals(advert, actualAdvert);
    }

    @Test
    public void findAdvertsWithAuth() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        User user = advert.getAuthor();
        doReturn(user).when(userRepository).findByUsername(any());
        doReturn(authentication).when(auth).getAuth();
        doReturn(List.of(advert)).when(advertRepository).findByAuthorId(anyInt());
        Method method = advertService
                .getClass()
                .getDeclaredMethod("findAdvertsWithAuth");
        method.setAccessible(true);
        List<Advert> actualAdverts = (List<Advert>) method.invoke(advertService);
        assertNotNull(actualAdverts);
        assertEquals(List.of(advert), actualAdverts);
    }

//    @Test
//    public void deleteByAdmin() {
//        User user = advert.getAuthor();
//        doReturn(user).when(userRepository).findByUsername(any());
//        doReturn(Optional.of(advert)).when(advertRepository).findById(anyInt());
//        advertService.deleteByAdmin(advert.getId(), authentication);
//        verify(advertRepository, times(1)).delete(any());
//    }

    @Test
    public void DoesThrowUserUnauthorizedExceptionWhenFindAdvertsWithAuth(){
        doReturn(null).when(userRepository).findByUsername(any());
        doReturn(authentication).when(auth).getAuth();
        assertThrows(UserUnauthorizedException.class,
                () -> advertService.findAllByAuthUser());
    }

    @Test
    public void DoesThrowAdvertNotFoundExceptionExceptionWhenFindAdvertWithAuth() {
        doReturn(Optional.empty()).when(advertRepository).findById(anyInt());
        assertThrows(AdvertNotFoundException.class,
                () -> advertService.updateImage(anyInt(), mockMultipartFile));
    }

    @Test
    public void DoesThrowActionForbiddenExceptionWhenFindAdvertWithAuth() {
        boolean isAuthenticationNull = true;
        doReturn(Optional.of(advert)).when(advertRepository).findById(anyInt());
        doReturn(isAuthenticationNull).when(auth).checkAuthNotEnough(any());
        assertThrows(ActionForbiddenException.class,
                () -> advertService.updateImage(anyInt(), mockMultipartFile));
    }
}
