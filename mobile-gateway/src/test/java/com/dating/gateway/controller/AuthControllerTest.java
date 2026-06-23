package com.dating.gateway.controller;

import com.dating.gateway.dto.LoginDeviceReq;
import com.dating.gateway.dto.vo.LoginResultVO;
import com.dating.gateway.dto.vo.SendSmsCodeVO;
import com.dating.gateway.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @Test
    void sendSmsCode_shouldReturnMockCode() throws Exception {
        SendSmsCodeVO vo = new SendSmsCodeVO();
        vo.setMockCode("123456");
        when(authService.sendSmsCode(any())).thenReturn(vo);

        mockMvc.perform(post("/api/v1/auth/send-sms-code")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"phone\":\"+8613812345678\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.mockCode").value("123456"));
    }

    @Test
    void loginDevice_shouldReturnLoginResult() throws Exception {
        LoginResultVO vo = new LoginResultVO();
        vo.setUserId(10001L);
        vo.setAccessToken("access");
        vo.setRefreshToken("refresh");
        vo.setPending(true);
        vo.setNewlyCreated(true);
        vo.setAccessExpiresAtMs(1000L);
        vo.setRefreshExpiresAtMs(2000L);
        when(authService.loginDevice(any())).thenReturn(vo);

        LoginDeviceReq req = new LoginDeviceReq();
        req.setDeviceId("dev-device-001");
        req.setPlatform(3);

        mockMvc.perform(post("/api/v1/auth/login-device")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.userId").value(10001))
                .andExpect(jsonPath("$.data.accessToken").value("access"));
    }
}
