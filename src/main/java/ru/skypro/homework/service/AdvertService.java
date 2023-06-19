package ru.skypro.homework.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import ru.skypro.homework.dto.AdsDto;
import ru.skypro.homework.dto.CreateAdsDto;
import ru.skypro.homework.dto.FullAdsDto;
import ru.skypro.homework.dto.ResponseWrapperAdsDto;
import ru.skypro.homework.exception.AdvertNotFoundException;
import ru.skypro.homework.exception.UserUnauthorizedException;
import ru.skypro.homework.mapper.AdvertMapper;
import ru.skypro.homework.model.Advert;
import ru.skypro.homework.model.Image;
import ru.skypro.homework.model.User;
import ru.skypro.homework.repository.AdvertRepository;
import ru.skypro.homework.repository.UserRepository;

import java.io.IOException;
import java.util.List;

/**
 * service for maintain adverts via {@link AdvertRepository}
 */
@Service
@Slf4j
public class AdvertService {
    private final AdvertRepository advertRepository;
    private final AdvertMapper advertMapper;
    private final UserRepository userRepository;
    private final PhotoService photoService;

    public AdvertService(AdvertRepository advertRepository,
                         AdvertMapper advertMapper,
                         UserRepository userRepository,
                         PhotoService photoService) {
        this.advertRepository = advertRepository;
        this.advertMapper = advertMapper;
        this.userRepository = userRepository;
        this.photoService = photoService;
    }

    /**
     * Create advert via {@link AdvertRepository}
     *
     * @param properties data to create advert
     * @param file       image file
     * @return advert DTO object
     */
    @Transactional
    public AdsDto create(Authentication auth, CreateAdsDto properties, MultipartFile file) {
        log.info("Creat advert with properties: " + properties);
        Image image = photoService.uploadImage(file);
        Advert advert = advertMapper.createAdsDtoToAdvert(properties);
        advert.setAuthor(userRepository.findByEmail(auth.getName()));
        advert.setImage(image);
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
        Advert advert = advertRepository.findById(id)
                .orElseThrow(() -> new AdvertNotFoundException("Advert not found"));
        advertRepository.delete(advert);
    }

    /**
     * Update advert via {@link AdvertRepository}
     *
     * @param id         advert id
     * @param properties data to update
     * @return advert DTO object
     */
    @Transactional
    public AdsDto update(int id, CreateAdsDto properties) {
        log.info("Update advert with id: " + id);
        Advert advert = advertRepository.findById(id)
                .orElseThrow(() -> new AdvertNotFoundException("Advert not found"));
        advertMapper.updateAdvert(properties, advert);
        advertRepository.save(advert);
        return advertMapper.advertToAdsDto(advert);
    }

    /**
     * Update advert image
     *
     * @param id   advert id
     * @param file image file
     */
    @Transactional
    public byte[] updateImage(int id, MultipartFile file) throws IOException {
        log.info("Update advert image with id: " + id);
        Advert advert = advertRepository.findById(id)
                .orElseThrow(() -> new AdvertNotFoundException("Advert not found"));
        photoService.uploadImage(advert, file);
        return file.getBytes();
    }

    /**
     * Download advert image
     *
     * @param id advert id
     * @return image
     */
    public Image downloadImage(int id) {
        log.info("Download advert image with id: " + id);
        Advert advert = advertRepository.findById(id)
                .orElseThrow(() -> new AdvertNotFoundException("Advert not found"));
        return advert.getImage();
    }

    /**
     * Find all adverts via {@link AdvertRepository}
     *
     * @return list of adverts
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
     * @return advert DTO object
     */
    public FullAdsDto findById(int id) {
        log.info("Find advert by id: " + id);
        Advert advert = advertRepository.findById(id)
                .orElseThrow(() -> new AdvertNotFoundException("Advert not found"));
        return advertMapper.advertToFullAdsDto(advert);
    }

    /**
     * Find all adverts for authorized user via {@link AdvertRepository} and {@link UserRepository}
     *
     * @param auth authorized user data
     * @return list of adverts
     */
    public ResponseWrapperAdsDto findAllByAuthUser(Authentication auth) {
        log.info("Find adverts by user name");
        User user = userRepository.findByEmail(auth.getName());
        if (user == null) {
            throw new UserUnauthorizedException("user not found");
        }
        List<Advert> adverts = advertRepository.findByAuthorId(user.getId());
        return advertMapper.listToRespWrapperAdsDto(adverts);
    }
}
