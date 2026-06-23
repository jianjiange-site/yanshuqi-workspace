package com.dating.gateway.controller;

import com.dating.gateway.dto.vo.HomeCardVO;
import com.dating.gateway.dto.vo.UserProfileVO;
import com.dating.gateway.resolver.CallerUserResolver;
import com.dating.gateway.service.HomeBffService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(HomeController.class)
@AutoConfigureMockMvc(addFilters = false)
class HomeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private HomeBffService homeBffService;

    @MockBean
    private CallerUserResolver callerUserResolver;

    @Test
    void card_shouldReturnHomeCard() throws Exception {
        when(callerUserResolver.resolveCallerUserId(any())).thenReturn(10001L);
        HomeCardVO vo = new HomeCardVO();
        vo.setSelfUserId(10001L);
        UserProfileVO target = new UserProfileVO();
        target.setUserId(30003L);
        target.setNickname("Target");
        vo.setTarget(target);
        when(homeBffService.getHomeCard(10001L, 30003L)).thenReturn(vo);

        mockMvc.perform(get("/api/v1/home/card").param("targetId", "30003"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.selfUserId").value(10001))
                .andExpect(jsonPath("$.data.target.userId").value(30003))
                .andExpect(jsonPath("$.data.target.nickname").value("Target"));

        verify(homeBffService).getHomeCard(10001L, 30003L);
    }

    @Test
    void card_missingTargetId_shouldFail() throws Exception {
        when(callerUserResolver.resolveCallerUserId(any())).thenReturn(10001L);

        mockMvc.perform(get("/api/v1/home/card"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(10400));
    }
}
