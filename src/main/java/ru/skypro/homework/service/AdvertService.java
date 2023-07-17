package ru.skypro.homework.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import ru.skypro.homework.component.AuthenticationComponent;
import ru.skypro.homework.dto.AdsDto;
import ru.skypro.homework.dto.CreateAdsDto;
import ru.skypro.homework.dto.FullAdsDto;
import ru.skypro.homework.dto.ResponseWrapperAdsDto;
import ru.skypro.homework.exception.*;
import ru.skypro.homework.mapper.AdvertMapper;
import ru.skypro.homework.model.Advert;
import ru.skypro.homework.model.Image;
import ru.skypro.homework.model.Photo;
import ru.skypro.homework.model.User;
import ru.skypro.homework.repository.AdvertRepository;
import ru.skypro.homework.repository.UserRepository;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

/**
 * Service for maintain adverts via {@link AdvertRepository}
 */
@Service
@Slf4j
public class AdvertService {
    private final AdvertRepository advertRepository;
    private final AdvertMapper advertMapper;
    private final UserRepository userRepository;
    private final ImageService imageService;
    private final AuthenticationComponent auth;

    public AdvertService(AdvertRepository advertRepository,
                         AdvertMapper advertMapper,
                         UserRepository userRepository,
                         ImageService imageService,
                         AuthenticationComponent auth) {
        this.advertRepository = advertRepository;
        this.advertMapper = advertMapper;
        this.userRepository = userRepository;
        this.imageService = imageService;
        this.auth = auth;
    }

    /**
     * Create advert via {@link AdvertRepository}
     *
     * @param properties {@link CreateAdsDto}
     * @param file       {@link MultipartFile}
     * @return {@link AdsDto}
     */
    @Transactional
    public AdsDto create(CreateAdsDto properties, MultipartFile file) {
        log.info("Creat advert with properties: " + properties);
        Photo photo = imageService.uploadPhoto(file);
        Advert advert = advertMapper.createAdsDtoToAdvert(properties);
        advert.setAuthor(userRepository.findByUsername(auth.getAuth().getName()));
        advert.setPhoto(photo);
        return advertMapper.advertToAdsDto(advertRepository.save(advert));
    }

    /**
     * Delete advert by Id via {@link AdvertRepository}
     *
     * @param id advert id
     */
    @Transactional
    public void delete(int id) {
        log.info("Delete advert with id: " + id);
        Advert advert = findAdvertWithAuth(id);
        Photo photo = advert.getPhoto();
        advertRepository.delete(advert);
        imageService.deleteFile(photo);
    }

    /**
     * Update advert via {@link AdvertRepository}
     *
     * @param id         advert id
     * @param properties {@link CreateAdsDto}
     * @return {@link AdsDto}
     */
    @Transactional
    public AdsDto update(int id, CreateAdsDto properties) {
        log.info("Update advert with id: " + id);
        Advert advert = findAdvertWithAuth(id);
        advertMapper.updateAdvert(properties, advert);
        advertRepository.save(advert);
        return advertMapper.advertToAdsDto(advert);
    }

    /**
     * Update advert image
     *
     * @param id   advert id
     * @param file {@link MultipartFile}
     * @return image bytes
     */
    @Transactional
    public byte[] updateImage(int id, MultipartFile file) {
        log.info("Update advert image with id: " + id);
        try {
            Advert advert = findAdvertWithAuth(id);
            imageService.uploadPhoto(advert, file);
            return file.getBytes();
        } catch (IOException exception) {
            log.error(exception.getMessage());
            throw new ImageUploadException(exception.getMessage());
        }
    }

    /**
     * Download advert image
     *
     * @param id advert id
     * @return {@link Image}
     */
    public Image downloadImage(int id) {
        log.info("Download advert image with id: " + id);
        Advert advert = findAdvert(id);
        return advert.getPhoto();
    }

    /**
     * Find all adverts via {@link AdvertRepository}
     *
     * @return {@link ResponseWrapperAdsDto}
     */
    public ResponseWrapperAdsDto findAll() {
        log.info("Find all adverts");
        List<Advert> adverts = advertRepository.findAll();
        return advertMapper.listToRespWrapperAdsDto(adverts);
    }

    /**
     * Find advert by id via {@link AdvertRepository}
     *
     * @param id advert id
     * @return {@link FullAdsDto}
     */
    public FullAdsDto findById(int id) {
        log.info("Find advert by id: " + id);
        Advert advert = findAdvert(id);
        return advertMapper.advertToFullAdsDto(advert);
    }

    /**
     * Find all adverts for authorized user via {@link AdvertRepository} and {@link UserRepository}
     *
     * @return {@link ResponseWrapperAdsDto}
     */
    public ResponseWrapperAdsDto findAllByAuthUser() {
        log.info("Find adverts by user name");
        List<Advert> adverts = findAdvertsWithAuth();
        return advertMapper.listToRespWrapperAdsDto(adverts);
    }

    private Advert findAdvert(int id) {
        return advertRepository.findById(id).orElseThrow(() -> new AdvertNotFoundException("Advert not found"));
    }

    private Advert findAdvertWithAuth(int id) {
        Optional<Advert> advert = advertRepository.findById(id);
        if (advert.isEmpty()) {
            throw new AdvertNotFoundException("Advert not found");
        }
        if (auth.checkAuthNotEnough(advert.get().getAuthor().getUsername())) {
            throw new ActionForbiddenException("Forbidden");
        }
        return advert.get();
    }

    private List<Advert> findAdvertsWithAuth() {
        User user = userRepository.findByUsername(auth.getAuth().getName());
        if (user == null) {
            throw new UserUnauthorizedException("User not found");
        }
        return advertRepository.findByAuthorId(user.getId());
    }
}
