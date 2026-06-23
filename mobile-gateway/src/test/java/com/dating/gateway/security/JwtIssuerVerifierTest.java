package com.dating.gateway.security;

import com.dating.gateway.exception.GatewayBizException;
import com.dating.gateway.exception.GatewayErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtIssuerVerifierTest {

    private JwtProperties jwtProperties;
    private JwtKeyProvider jwtKeyProvider;
    private JwtIssuer jwtIssuer;
    private JwtVerifier jwtVerifier;

    @BeforeEach
    void setUp() {
        jwtProperties = new JwtProperties();
        jwtProperties.setIssuer("dating-mobile-gateway-test");
        jwtProperties.setAccessTokenTtlSeconds(900);
        jwtProperties.setRefreshTokenTtlSeconds(604800);

        MockEnvironment environment = new MockEnvironment();
        environment.setActiveProfiles("test");

        jwtKeyProvider = new JwtKeyProvider(jwtProperties, environment);
        jwtKeyProvider.init();
        jwtIssuer = new JwtIssuer(jwtProperties, jwtKeyProvider);
        jwtVerifier = new JwtVerifier(jwtProperties, jwtKeyProvider);
    }

    @Test
    void issueAndVerify_shouldContainExpectedClaims() {
        TokenPair tokenPair = jwtIssuer.issueTokenPair(20002L, "dev-device-001", 3, 1);
        JwtClaims claims = jwtVerifier.verifyAccessToken(tokenPair.getAccessToken());

        assertEquals(20002L, claims.getUserId());
        assertEquals("dev-device-001", claims.getDeviceId());
        assertEquals(3, claims.getPlatform());
        assertEquals(1, claims.getTokenVersion());
        assertTrue(claims.getJti().length() >= 16);
    }

    @Test
    void verifyTamperedToken_shouldFail() {
        TokenPair tokenPair = jwtIssuer.issueTokenPair(20002L, "dev-device-001", 3, null);
        String tampered = tokenPair.getAccessToken().substring(0, tokenPair.getAccessToken().length() - 4) + "abcd";
        GatewayBizException ex = assertThrows(GatewayBizException.class, () -> jwtVerifier.verifyAccessToken(tampered));
        assertEquals(GatewayErrorCode.TOKEN_INVALID, ex.getErrorCode());
    }
}
