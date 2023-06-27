package ru.skypro.homework.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import ru.skypro.homework.component.AuthenticationComponent;
import ru.skypro.homework.dto.AdsDto;
import ru.skypro.homework.dto.CreateAdsDto;
import ru.skypro.homework.dto.FullAdsDto;
import ru.skypro.homework.dto.ResponseWrapperAdsDto;
import ru.skypro.homework.exception.ActionForbiddenException;
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
import java.util.Optional;

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
    private final AuthenticationComponent auth;

    public AdvertService(AdvertRepository advertRepository,
                         AdvertMapper advertMapper,
                         UserRepository userRepository,
                         PhotoService photoService,
                         AuthenticationComponent auth) {
        this.advertRepository = advertRepository;
        this.advertMapper = advertMapper;
        this.userRepository = userRepository;
        this.photoService = photoService;
        this.auth = auth;
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
        advert.setAuthor(userRepository.findByUsername(auth.getName()));
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
        Advert advert = findAdvertWithAuth(id);
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
        Advert advert = findAdvertWithAuth(id);
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
        Advert advert = findAdvertWithAuth(id);
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
        Advert advert = findAdvert(id);
        return advertMapper.advertToFullAdsDto(advert);
    }

    /**
     * Find all adverts for authorized user via {@link AdvertRepository} and {@link UserRepository}
     *
     * @return list of adverts
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
        if (!advert.isPresent()) {
            throw new AdvertNotFoundException("Advert not found");
        }
        if (auth.check(advert.get().getAuthor().getUsername())) {
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

    public void deleteByAdmin(int id, Authentication authentication) {
        User user = userRepository.findByUsername(authentication.getName());
        if (user.getRole().getAuthority().equals("ROLE_ADMIN")) {
            Optional<Advert> advert = advertRepository.findById(id);
            advertRepository.delete(advert.get());
        }
    }
}
