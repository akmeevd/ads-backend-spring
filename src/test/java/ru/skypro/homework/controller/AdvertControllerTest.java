package ru.skypro.homework.controller;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import ru.skypro.homework.dto.*;
import ru.skypro.homework.model.Advert;
import ru.skypro.homework.model.Image;
import ru.skypro.homework.service.AdvertService;

import java.nio.file.Path;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@WithMockUser(roles = "USER")
public class AdvertControllerTest {
    @Autowired
    private WebApplicationContext webApplicationContext;
    private MockMvc mockMvc;
    @MockBean
    private AdvertService advertService;
    private JSONObject createAdsDto;
    private MockMultipartFile file1, file2;
    private Advert advert;

    @BeforeEach
    public void setup() throws JSONException {
        advert = new Advert();
        advert.setId(1);
        createAdsDto = new JSONObject();
        createAdsDto.put("title", "title");
        createAdsDto.put("description", "desc");
        createAdsDto.put("price", 1000);
        file1 = new MockMultipartFile("properties",null,
                MediaType.APPLICATION_JSON_VALUE,
                "{\"title\":\"title\"}".getBytes());
        mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();
        byte[] bytes = {1};
        file2 = new MockMultipartFile("image", null,
                MediaType.IMAGE_JPEG_VALUE, bytes);
    }

    @Test
    public void delete() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.delete("/ads/" + advert.getId()))
                .andExpect(status().isNoContent());
    }

    @Test
    public void update() throws Exception {
        AdsDto adsDto = new AdsDto();
        adsDto.setTitle("title");
        adsDto.setPrice(1000);
        Mockito.doReturn(adsDto).when(advertService).update(anyInt(), any());
        mockMvc.perform(patch("/ads/" + advert.getId())
                        .content(createAdsDto.toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.price").value(adsDto.getPrice()))
                .andExpect(jsonPath("$.title").value(adsDto.getTitle()));
    }

    @Test
    public void create() throws Exception {
        AdsDto adsDto = new AdsDto();
        adsDto.setTitle("title");
        adsDto.setPrice(1000);
        Mockito.doReturn(adsDto).when(advertService).create(any(), any(), any());
        mockMvc.perform(multipart("/ads")
                        .file(file1)
                        .file(file2))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.price").value(adsDto.getPrice()))
                .andExpect(jsonPath("$.title").value(adsDto.getTitle()));
    }

    @Test
    public void updateImage() throws Exception {
        mockMvc.perform(multipart(HttpMethod.PATCH,"/ads/" + advert.getId() + "/image")
                        .file(file2))
                .andExpect(status().isOk());
    }

    @Test
    public void findById() throws Exception {
        FullAdsDto fullAdsDto = new FullAdsDto();
        fullAdsDto.setPk(1);
        Mockito.doReturn(fullAdsDto).when(advertService).findById(anyInt());
        mockMvc.perform(get("/ads/" + advert.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pk").value(fullAdsDto.getPk()));
    }

    @Test
    public void findAllByAuthUser() throws Exception {
        AdsDto adsDto = new AdsDto();
        adsDto.setPk(1);
        ResponseWrapperAdsDto wrapperAdsDto = new ResponseWrapperAdsDto();
        wrapperAdsDto.setCount(1);
        wrapperAdsDto.setResults(List.of(adsDto));
        Mockito.doReturn(wrapperAdsDto).when(advertService).findAllByAuthUser();
        mockMvc.perform(get("/ads/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count").value(wrapperAdsDto.getCount()))
                .andExpect(jsonPath("$.results[0]").value(adsDto));
    }

    @Test
    public void findAll() throws Exception {
        AdsDto adsDto1 = new AdsDto();
        adsDto1.setPk(1);
        AdsDto adsDto2 = new AdsDto();
        adsDto2.setPk(2);
        ResponseWrapperAdsDto wrapperAdsDto = new ResponseWrapperAdsDto();
        wrapperAdsDto.setCount(2);
        wrapperAdsDto.setResults(List.of(adsDto1,adsDto2));
        Mockito.doReturn(wrapperAdsDto).when(advertService).findAll();
        mockMvc.perform(get("/ads"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count").value(wrapperAdsDto.getCount()))
                .andExpect(jsonPath("$.results[0]").value(adsDto1))
                .andExpect(jsonPath("$.results[1]").value(adsDto2));
    }

    @Test
    public void downloadImage() throws Exception {
        Image image = new Image();
        Path path = Path.of("src\\test\\resources\\picture");
        image.setId(1);
        image.setPhotoDir(path.toAbsolutePath().toString());
        image.setFileExtension("jpeg");
        Mockito.doReturn(image).when(advertService).downloadImage(anyInt());
        mockMvc.perform(get("/ads/" + advert.getId() + "/image"))
                .andExpect(status().isOk());
    }
}
