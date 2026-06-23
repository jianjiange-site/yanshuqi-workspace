package com.dating.gateway.controller;

import com.dating.gateway.dto.vo.UserProfileVO;
import com.dating.gateway.resolver.CallerUserResolver;
import com.dating.gateway.resolver.DevHeaderCallerUserResolver;
import com.dating.gateway.service.ProfileBffService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProfileController.class)
@AutoConfigureMockMvc(addFilters = false)
class ProfileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProfileBffService profileBffService;

    @MockBean
    private CallerUserResolver callerUserResolver;

    @Test
    void upsertOnboarding_shouldReturnUserProfile() throws Exception {
        when(callerUserResolver.resolveCallerUserId(any())).thenReturn(10001L);
        UserProfileVO vo = new UserProfileVO();
        vo.setUserId(10001L);
        vo.setNickname("测试用户");
        vo.setGender(1);
        when(profileBffService.upsertOnboarding(eq(10001L), any())).thenReturn(vo);

        mockMvc.perform(post("/api/v1/profile/onboarding")
                        .header(DevHeaderCallerUserResolver.HEADER_USER_ID, "10001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nickname":"测试用户","gender":1,"birthday":"1998-05-20","age":26}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.userId").value(10001))
                .andExpect(jsonPath("$.data.nickname").value("测试用户"));

        verify(profileBffService).upsertOnboarding(eq(10001L), any());
    }

    @Test
    void updateProfile_shouldReturnTrue() throws Exception {
        when(callerUserResolver.resolveCallerUserId(any())).thenReturn(10001L);
        when(profileBffService.updateProfile(eq(10001L), any())).thenReturn(true);

        mockMvc.perform(patch("/api/v1/profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nickname\":\"测试用户2\",\"bio\":\"updated\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data").value(true));

        verify(profileBffService).updateProfile(eq(10001L), any());
    }

    @Test
    void withoutJwt_resolverThrows_shouldReturnBizError() throws Exception {
        when(callerUserResolver.resolveCallerUserId(any()))
                .thenThrow(new com.dating.gateway.exception.GatewayBizException(
                        com.dating.gateway.exception.GatewayErrorCode.TOKEN_INVALID));

        mockMvc.perform(post("/api/v1/profile/onboarding")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nickname\":\"test\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(10501));
    }

    @Test
    void devHeaderFallback_resolverUsesCallerUserId() throws Exception {
        when(callerUserResolver.resolveCallerUserId(any())).thenReturn(20002L);
        when(profileBffService.upsertOnboarding(eq(20002L), any())).thenReturn(new UserProfileVO());

        mockMvc.perform(post("/api/v1/profile/onboarding")
                        .header(DevHeaderCallerUserResolver.HEADER_USER_ID, "20002")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nickname\":\"dev\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        verify(profileBffService).upsertOnboarding(eq(20002L), any());
    }
}
