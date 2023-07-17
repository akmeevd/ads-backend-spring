package ru.skypro.homework.controller;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import ru.skypro.homework.dto.CommentDto;
import ru.skypro.homework.dto.ResponseWrapperCommentDto;
import ru.skypro.homework.model.Advert;
import ru.skypro.homework.service.CommentService;
import java.util.List;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@WithMockUser(roles = "USER")
public class CommentControllerTest {

    @Autowired
    private WebApplicationContext context;
    private MockMvc mockMvc;
    @MockBean
    private CommentService commentService;
    private JSONObject jsonCommentDto;
    private CommentDto commentDto;
    private Advert advert;

    @BeforeEach
    public void setup() throws JSONException {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();
        advert = new Advert();
        advert.setId(1);
        commentDto = new CommentDto();
        commentDto.setPk(1);
        commentDto.setAuthorImage("image");
        commentDto.setAuthor(1);
        commentDto.setText("text");
        commentDto.setAuthorFirstName("name");
        commentDto.setCreatedAt(1011970);
        jsonCommentDto = new JSONObject();
        jsonCommentDto.put("pk", commentDto.getPk());
        jsonCommentDto.put("text", commentDto.getText());
        jsonCommentDto.put("createdAt", commentDto.getCreatedAt());
        jsonCommentDto.put("author", commentDto.getAuthor());
        jsonCommentDto.put("authorFirstName", commentDto.getAuthorFirstName());
        jsonCommentDto.put("authorImage", commentDto.getAuthorImage());
    }

    @Test
    public void create() throws Exception {
        doReturn(commentDto).when(commentService).create(anyInt(), any());
        mockMvc.perform(post("/ads/" + advert.getId() + "/comments")
                        .content(jsonCommentDto.toString())
                        .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.pk").value(commentDto.getPk()))
                .andExpect(jsonPath("$.text").value(commentDto.getText()))
                .andExpect(jsonPath("$.createdAt").value(commentDto.getCreatedAt()))
                .andExpect(jsonPath("$.author").value(commentDto.getAuthor()))
                .andExpect(jsonPath("$.authorFirstName").value(commentDto.getAuthorFirstName()))
                .andExpect(jsonPath("$.authorImage").value(commentDto.getAuthorImage()));
    }

    @Test
    public void delete() throws Exception {
        doNothing().when(commentService).delete(anyInt(), any());
        mockMvc.perform(MockMvcRequestBuilders
                        .delete("/ads/" + advert.getId() + "/comments/" + commentDto.getPk()))
                .andExpect(status().isOk());
    }

    @Test
    public void update() throws Exception {
        doReturn(commentDto).when(commentService).update(anyInt(), anyInt(), any());
        mockMvc.perform(patch("/ads/" + advert.getId() + "/comments/" + commentDto.getPk())
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(jsonCommentDto.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pk").value(commentDto.getPk()))
                .andExpect(jsonPath("$.text").value(commentDto.getText()))
                .andExpect(jsonPath("$.createdAt").value(commentDto.getCreatedAt()))
                .andExpect(jsonPath("$.author").value(commentDto.getAuthor()))
                .andExpect(jsonPath("$.authorFirstName").value(commentDto.getAuthorFirstName()))
                .andExpect(jsonPath("$.authorImage").value(commentDto.getAuthorImage()));
    }

    @Test
    public void findAllByAdvert() throws Exception {
        ResponseWrapperCommentDto wrapperCommentDto = new ResponseWrapperCommentDto();
        wrapperCommentDto.setCount(1);
        wrapperCommentDto.setResults(List.of(commentDto));
        doReturn(wrapperCommentDto).when(commentService).findAll(anyInt());
        mockMvc.perform(get("/ads/" + advert.getId() + "/comments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count").value(wrapperCommentDto.getCount()))
                .andExpect(jsonPath("$.results[0]").value(commentDto));
    }
}
