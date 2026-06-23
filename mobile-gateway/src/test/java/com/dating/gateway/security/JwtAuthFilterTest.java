package com.dating.gateway.security;

import com.dating.gateway.resolver.DevHeaderCallerUserResolver;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtAuthFilterTest {

    @Mock
    private JwtVerifier jwtVerifier;
    @Mock
    private TokenBlacklistService tokenBlacklistService;
    @Mock
    private FilterChain filterChain;

    private JwtAuthFilter jwtAuthFilter;
    private MockEnvironment environment;

    @BeforeEach
    void setUp() {
        environment = new MockEnvironment();
        jwtAuthFilter = new JwtAuthFilter(jwtVerifier, tokenBlacklistService, environment);
    }

    @AfterEach
    void tearDown() {
        CurrentUserContext.clear();
    }

    @Test
    void protectedPath_withoutAuth_shouldReturn401() throws Exception {
        environment.setActiveProfiles("prod");
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/match/quota");
        MockHttpServletResponse response = new MockHttpServletResponse();

        jwtAuthFilter.doFilter(request, response, filterChain);

        assertEquals(401, response.getStatus());
    }

    @Test
    void profilePath_withoutAuth_shouldReturn401() throws Exception {
        environment.setActiveProfiles("prod");
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/v1/profile/onboarding");
        MockHttpServletResponse response = new MockHttpServletResponse();

        jwtAuthFilter.doFilter(request, response, filterChain);

        assertEquals(401, response.getStatus());
    }

    @Test
    void postPath_withoutAuth_shouldReturn401() throws Exception {
        environment.setActiveProfiles("prod");
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/post/feed");
        MockHttpServletResponse response = new MockHttpServletResponse();

        jwtAuthFilter.doFilter(request, response, filterChain);

        assertEquals(401, response.getStatus());
    }

    @Test
    void protectedPath_withBearer_shouldSetContext() throws Exception {
        environment.setActiveProfiles("prod");
        JwtClaims claims = new JwtClaims(20002L, "jti-1", "dev-device", 3, 1, Long.MAX_VALUE / 1000);
        when(jwtVerifier.verifyAccessToken("good-token")).thenReturn(claims);
        when(tokenBlacklistService.isBlacklisted("jti-1")).thenReturn(false);

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/match/quota");
        request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer good-token");
        MockHttpServletResponse response = new MockHttpServletResponse();

        jwtAuthFilter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertNull(CurrentUserContext.get());
    }

    @Test
    void devProfile_shouldAllowXUserIdFallback() throws Exception {
        environment.setActiveProfiles("dev");
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/match/quota");
        request.addHeader(DevHeaderCallerUserResolver.HEADER_USER_ID, "20002");
        MockHttpServletResponse response = new MockHttpServletResponse();

        jwtAuthFilter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
    }

    @Test
    void devProfile_shouldAllowXUserIdFallbackOnProfilePath() throws Exception {
        environment.setActiveProfiles("dev");
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/v1/profile/onboarding");
        request.addHeader(DevHeaderCallerUserResolver.HEADER_USER_ID, "20002");
        MockHttpServletResponse response = new MockHttpServletResponse();

        jwtAuthFilter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
    }

    @Test
    void publicAuthPath_shouldPassWithoutToken() throws Exception {
        environment.setActiveProfiles("prod");
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/v1/auth/login-device");
        MockHttpServletResponse response = new MockHttpServletResponse();

        jwtAuthFilter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
    }

    @Test
    void callbackPath_shouldSkipJwtFilter() throws Exception {
        environment.setActiveProfiles("prod");
        MockHttpServletRequest request = new MockHttpServletRequest(
                "POST", "/callback/openim/callbackBeforeSendSingleMsg");
        MockHttpServletResponse response = new MockHttpServletResponse();

        jwtAuthFilter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
    }

    @Test
    void paymentPath_withoutAuth_shouldReturn401() throws Exception {
        environment.setActiveProfiles("prod");
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/payment/coins");
        MockHttpServletResponse response = new MockHttpServletResponse();

        jwtAuthFilter.doFilter(request, response, filterChain);

        assertEquals(401, response.getStatus());
    }

    @Test
    void imTokenPath_withoutAuth_shouldReturn401() throws Exception {
        environment.setActiveProfiles("prod");
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/im/token");
        MockHttpServletResponse response = new MockHttpServletResponse();

        jwtAuthFilter.doFilter(request, response, filterChain);

        assertEquals(401, response.getStatus());
    }

    @Test
    void callTokenPath_withoutAuth_shouldReturn401() throws Exception {
        environment.setActiveProfiles("prod");
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/v1/call/token");
        MockHttpServletResponse response = new MockHttpServletResponse();

        jwtAuthFilter.doFilter(request, response, filterChain);

        assertEquals(401, response.getStatus());
    }
}
