package com.dating.gateway.controller;

import com.dating.gateway.config.AuthConfiguration;
import com.dating.gateway.resolver.JwtCallerUserResolver;
import com.dating.gateway.security.JwtClaims;
import com.dating.gateway.security.JwtVerifier;
import com.dating.gateway.security.TokenBlacklistService;
import com.dating.gateway.service.PaymentBffService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = PaymentController.class)
@Import({AuthConfiguration.class, JwtCallerUserResolver.class})
@ActiveProfiles("prod")
class PaymentControllerJwtTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PaymentBffService paymentBffService;

    @MockBean
    private JwtVerifier jwtVerifier;

    @MockBean
    private TokenBlacklistService tokenBlacklistService;

    @Test
    void withoutJwt_shouldReturn401() throws Exception {
        mockMvc.perform(get("/api/v1/payment/coins"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(10501));
    }

    @Test
    void withJwt_shouldReachService() throws Exception {
        mockJwtUser(70001L);

        mockMvc.perform(get("/api/v1/payment/coins")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer good-token"))
                .andExpect(status().isOk());

        verify(paymentBffService).getCoins(70001L);
    }

    private void mockJwtUser(long userId) {
        JwtClaims claims = new JwtClaims(userId, "jti-gw5-pay", "dev-device", 3, 1, Long.MAX_VALUE / 1000);
        when(jwtVerifier.verifyAccessToken("good-token")).thenReturn(claims);
        when(tokenBlacklistService.isBlacklisted("jti-gw5-pay")).thenReturn(false);
    }
}
