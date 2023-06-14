package ru.skypro.homework.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import ru.skypro.homework.dto.AdsDto;
import ru.skypro.homework.dto.CreateAdsDto;
import ru.skypro.homework.dto.FullAdsDto;
import ru.skypro.homework.dto.ResponseWrapperAdsDto;
import ru.skypro.homework.model.Advert;
import ru.skypro.homework.model.Image;

import java.util.List;

@Mapper(componentModel = "spring")
public interface AdvertMapper {
    static String imageToUrl(Image image) {
        if (image == null) {
            return "";
        }
        return "/ads/" + image.getId() + "/image";
    }

    Advert createAdsDtoToAdvert(CreateAdsDto createAdsDto);

    @Mapping(target = "pk", source = "id")
    @Mapping(target = "author", source = "author.id")
    AdsDto advertToAdsDto(Advert advert);

    @Mapping(target = "pk", source = "id")
    @Mapping(target = "authorFirstName", source = "author.firstName")
    @Mapping(target = "authorLastName", source = "author.lastName")
    @Mapping(target = "email", source = "author.email")
    @Mapping(target = "phone", source = "author.phone")
    FullAdsDto advertToFullAdsDto(Advert advert);

    List<AdsDto> advertListToAdsDtoList(List<Advert> adverts);

    void updateAdvert(CreateAdsDto createAdsDto, @MappingTarget Advert advert);

    default ResponseWrapperAdsDto listToRespWrapperAdsDto(List<Advert> adverts) {
        ResponseWrapperAdsDto result = new ResponseWrapperAdsDto();
        result.setCount(adverts.size());
        result.setResults(advertListToAdsDtoList(adverts));
        return result;
    }
}
