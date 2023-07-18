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
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import ru.skypro.homework.service.AuthService;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@SpringBootTest
public class AuthControllerTest {

    @Autowired
    private WebApplicationContext context;
    @MockBean
    private AuthService authService;
    private MockMvc mockMvc;
    private JSONObject jsonRegisterReqDto;
    private JSONObject jsonLoginReqDto;

    @BeforeEach
    public void setup() throws JSONException {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();
        jsonRegisterReqDto = new JSONObject();
        jsonRegisterReqDto.put("username", "name");
        jsonRegisterReqDto.put("password", "password");
        jsonLoginReqDto = new JSONObject();
        jsonLoginReqDto.put("username", "name");
        jsonLoginReqDto.put("password", "password");
        jsonLoginReqDto.put("firstName", "first");
        jsonLoginReqDto.put("lastName", "last");
    }

    @Test
    public void login() throws Exception {
        doReturn(true).when(authService).login(any(), any());
        mockMvc.perform(post("/login")
                        .content(jsonLoginReqDto.toString())
                        .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk());
    }

    @Test
    public void register() throws Exception {
        doReturn(true).when(authService).register(any(), any());
        mockMvc.perform(post("/register")
                        .content(jsonRegisterReqDto.toString())
                        .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk());
    }
}
