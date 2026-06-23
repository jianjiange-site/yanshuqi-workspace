package com.dating.gateway.controller;

import com.dating.gateway.config.AuthConfiguration;
import com.dating.gateway.resolver.JwtCallerUserResolver;
import com.dating.gateway.security.JwtClaims;
import com.dating.gateway.security.JwtVerifier;
import com.dating.gateway.security.TokenBlacklistService;
import com.dating.gateway.service.ImBffService;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ImControllerTest {

    @Nested
    @WebMvcTest(ImTokenController.class)
    @AutoConfigureMockMvc(addFilters = false)
    class TokenTests {

        @Autowired
        private MockMvc mockMvc;

        @MockBean
        private com.dating.gateway.resolver.CallerUserResolver callerUserResolver;

        @MockBean
        private ImBffService imBffService;

        @Test
        void imToken_shouldReturnNotReady() throws Exception {
            when(callerUserResolver.resolveCallerUserId(any())).thenReturn(10001L);
            when(imBffService.getImToken(10001L))
                    .thenThrow(com.dating.gateway.support.GatewayFeatureNotReadySupport.imNotReady());

            mockMvc.perform(get("/api/v1/im/token"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(10801));
        }

        @Test
        void callToken_shouldReturnNotReady() throws Exception {
            when(callerUserResolver.resolveCallerUserId(any())).thenReturn(10001L);
            when(imBffService.getCallToken(eq(10001L), any()))
                    .thenThrow(com.dating.gateway.support.GatewayFeatureNotReadySupport.callNotReady());

            mockMvc.perform(post("/api/v1/call/token")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"peerId\":20002}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(10802));
        }
    }

    @Nested
    @WebMvcTest(ImTokenController.class)
    @AutoConfigureMockMvc(addFilters = false)
    @Import(com.dating.gateway.service.impl.ImBffServiceImpl.class)
    @ActiveProfiles("prod")
    class ValidationTests {

        @Autowired
        private MockMvc mockMvc;

        @MockBean
        private com.dating.gateway.resolver.CallerUserResolver callerUserResolver;

        @Test
        void callToken_missingPeerId_shouldReturnInvalidArgument() throws Exception {
            when(callerUserResolver.resolveCallerUserId(any())).thenReturn(10001L);

            mockMvc.perform(post("/api/v1/call/token")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(10400))
                    .andExpect(jsonPath("$.message").value("peerId 不能为空"));
        }
    }

    @Nested
    @WebMvcTest(ImTokenController.class)
    @AutoConfigureMockMvc(addFilters = false)
    @Import(com.dating.gateway.service.impl.MockImBffServiceImpl.class)
    @ActiveProfiles("test")
    class MockProfileTests {

        @Autowired
        private MockMvc mockMvc;

        @MockBean
        private com.dating.gateway.resolver.CallerUserResolver callerUserResolver;

        @Test
        void imToken_shouldReturnMockToken() throws Exception {
            when(callerUserResolver.resolveCallerUserId(any())).thenReturn(10001L);

            mockMvc.perform(get("/api/v1/im/token"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(0))
                    .andExpect(jsonPath("$.data.userId").value("10001"))
                    .andExpect(jsonPath("$.data.imToken").value("mock-im-token-10001"));
        }
    }

    @Nested
    @WebMvcTest(controllers = ImTokenController.class)
    @Import({AuthConfiguration.class, JwtCallerUserResolver.class})
    @ActiveProfiles("prod")
    class JwtTests {

        @Autowired
        private MockMvc mockMvc;

        @MockBean
        private ImBffService imBffService;

        @MockBean
        private JwtVerifier jwtVerifier;

        @MockBean
        private TokenBlacklistService tokenBlacklistService;

        @Test
        void imToken_withoutJwt_shouldReturn401() throws Exception {
            mockMvc.perform(get("/api/v1/im/token"))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.code").value(10501));
        }

        @Test
        void callToken_withoutJwt_shouldReturn401() throws Exception {
            mockMvc.perform(post("/api/v1/call/token")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"peerId\":20002}"))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.code").value(10501));
        }
    }
}
