package ru.skypro.homework.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
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
import ru.skypro.homework.mapper.AdvertMapperImpl;
import ru.skypro.homework.model.*;
import ru.skypro.homework.repository.AdvertRepository;
import ru.skypro.homework.repository.UserRepository;

import java.io.*;
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
    private UserRepository userRepository;
    @Mock
    private ImageService imageService;
    @Mock
    private AuthenticationComponent auth;
    @Mock
    private Authentication authentication;
    @Spy
    private AdvertMapper advertMapper = new AdvertMapperImpl();

    @BeforeEach
    public void setup() {
    }

    @Test
    public void create() {
        //Given
        AdsDto expected = advertMapper.advertToAdsDto(mockAdvert());
        doReturn(authentication).when(auth).getAuth();
        doReturn(mockAdvert().getAuthor().getUsername()).when(authentication).getName();
        doReturn(mockAdvert().getPhoto()).when(imageService).uploadPhoto(any());
        doReturn(mockAdvert().getAuthor()).when(userRepository).findByUsername(mockAdvert().getAuthor().getUsername());
        doReturn(mockAdvert()).when(advertRepository).save(any());
        //When
        AdsDto actual = advertService.create(mockCreateAdsDto(), mockFile());
        //Then
        assertNotNull(actual);
        assertEquals(expected, actual);
        verify(imageService, times(1)).uploadPhoto(any());
        verify(userRepository, times(1)).findByUsername(mockAdvert().getAuthor().getUsername());
        verify(advertRepository, times(1)).save(any());
    }

    @Test
    public void delete() {
        //Given
        doReturn(Optional.of(mockAdvert())).when(advertRepository).findById(any());
        //When
        advertService.delete(mockAdvert().getId());
        //Then
        verify(advertRepository, times(1)).delete(any());
        verify(imageService, times(1)).deleteFile(any());
    }

    @Test
    public void update() {
        //Given
        AdsDto expected = advertMapper.advertToAdsDto(mockAdvert());
        doReturn(Optional.of(mockAdvert())).when(advertRepository).findById(any());
        //When
        AdsDto actual = advertService.update(1, mockCreateAdsDto());
        //Then
        assertNotNull(actual);
        assertEquals(expected, actual);
        verify(advertRepository, times(1)).save(any());
    }

    @Test
    public void updateImage() throws IOException {
        //Given
        doReturn(Optional.of(mockAdvert())).when(advertRepository).findById(any());
        doReturn(mockAdvert().getPhoto()).when(imageService).uploadPhoto(any(), any());
        //When
        byte[] actualImageBytes = advertService.updateImage(mockAdvert().getId(), mockFile());
        //Then
        assertNotNull(actualImageBytes);
        assertArrayEquals(mockFile().getBytes(), actualImageBytes);
    }

    @Test
    public void downloadImage() {
        //Given
        doReturn(Optional.of(mockAdvert())).when(advertRepository).findById(any());
        //When
        Image actualPhoto = advertService.downloadImage(mockAdvert().getId());
        //Then
        assertNotNull(actualPhoto);
        assertEquals(mockAdvert().getPhoto(), actualPhoto);
    }

    @Test
    public void findAll() {
        //Given
        AdsDto adsDto = new AdsDto();
        adsDto.setPk(mockAdvert().getId());
        ResponseWrapperAdsDto expected = new ResponseWrapperAdsDto();
        expected.setCount(1);
        expected.setResults(List.of(adsDto));
        doReturn(List.of(mockAdvert())).when(advertRepository).findAll();
        doReturn(expected).when(advertMapper).listToRespWrapperAdsDto(any());
        //When
        ResponseWrapperAdsDto actual = advertService.findAll();
        //Then
        assertEquals(expected, actual);
    }

    @Test
    public void findById() {
        //Given
        FullAdsDto expected = advertMapper.advertToFullAdsDto(mockAdvert());
        doReturn(Optional.of(mockAdvert())).when(advertRepository).findById(any());
        doReturn(expected).when(advertMapper).advertToFullAdsDto(any());
        //When
        FullAdsDto actualFullAdsDto = advertService.findById(mockAdvert().getId());
        //Then
        assertNotNull(actualFullAdsDto);
        assertEquals(expected, actualFullAdsDto);
    }

    @Test
    public void findAllByAuthUser() {
        //Given
        ResponseWrapperAdsDto expected = advertMapper.listToRespWrapperAdsDto(List.of(mockAdvert()));
        doReturn(mockAdvert().getAuthor()).when(userRepository).findByUsername(any());
        doReturn(authentication).when(auth).getAuth();
        doReturn(List.of(mockAdvert())).when(advertRepository).findByAuthorId(anyInt());
        doReturn(expected).when(advertMapper).listToRespWrapperAdsDto(List.of(mockAdvert()));
        //When
        ResponseWrapperAdsDto actual = advertService.findAllByAuthUser();
        //Then
        assertNotNull(actual);
        assertEquals(expected, actual);
    }

    @Test
    public void DoesThrowUserUnauthorizedExceptionWhenFindAdvertsWithAuth() {
        //Given
        doReturn(null).when(userRepository).findByUsername(any());
        doReturn(authentication).when(auth).getAuth();
        //Then
        assertThrows(UserUnauthorizedException.class,
                () -> advertService.findAllByAuthUser());
    }

    @Test
    public void DoesThrowAdvertNotFoundExceptionExceptionWhenFindAdvertWithAuth() {
        //Given
        doReturn(Optional.empty()).when(advertRepository).findById(anyInt());
        //Then
        assertThrows(AdvertNotFoundException.class,
                () -> advertService.updateImage(anyInt(), mockFile()));
    }

    @Test
    public void DoesThrowActionForbiddenExceptionWhenFindAdvertWithAuth() {
        //Given
        doReturn(Optional.of(mockAdvert())).when(advertRepository).findById(anyInt());
        doReturn(true).when(auth).checkAuthNotEnough(any());
        //Then
        assertThrows(ActionForbiddenException.class,
                () -> advertService.updateImage(anyInt(), mockFile()));
    }

    private Advert mockAdvert() {
        User user = new User();
        user.setId(1);
        user.setUsername("admin@ru");

        Photo photo = new Photo();
        photo.setId(1);
        photo.setFileName("file");
        photo.setFileExtension("jpeg");

        Advert advert = new Advert();
        advert.setId(1);
        advert.setTitle("title");
        advert.setDescription("descr");
        advert.setPrice(11);
        advert.setPhoto(photo);
        advert.setAuthor(user);
        return advert;
    }

    private CreateAdsDto mockCreateAdsDto() {
        CreateAdsDto createAdsDto = new CreateAdsDto();
        createAdsDto.setTitle(mockAdvert().getTitle());
        createAdsDto.setDescription(mockAdvert().getDescription());
        createAdsDto.setPrice(mockAdvert().getPrice());
        return createAdsDto;
    }

    private MockMultipartFile mockFile() {
        try {
            Resource resource = new ClassPathResource("picture/images.jpeg");
            return new MockMultipartFile(
                    "image",
                    "image.jpeg",
                    MediaType.IMAGE_JPEG_VALUE,
                    Files.readAllBytes(resource.getFile().toPath())
            );
        } catch (IOException exception) {
            throw new RuntimeException(exception.getMessage());
        }
    }
}
