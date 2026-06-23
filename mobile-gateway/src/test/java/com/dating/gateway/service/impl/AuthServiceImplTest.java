package com.dating.gateway.service.impl;

import com.dating.gateway.client.UserAuthGrpcClient;
import com.dating.gateway.dto.RefreshTokenReq;
import com.dating.gateway.entity.AuthRefreshTokenEntity;
import com.dating.gateway.exception.GatewayBizException;
import com.dating.gateway.exception.GatewayErrorCode;
import com.dating.gateway.manager.AuthDeviceManager;
import com.dating.gateway.manager.AuthRefreshTokenManager;
import com.dating.gateway.security.JwtIssuer;
import com.dating.gateway.security.TokenBlacklistService;
import com.dating.gateway.security.TokenPair;
import com.dating.gateway.service.SmsCodeService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserAuthGrpcClient userAuthGrpcClient;
    @Mock
    private AuthDeviceManager authDeviceManager;
    @Mock
    private AuthRefreshTokenManager authRefreshTokenManager;
    @Mock
    private JwtIssuer jwtIssuer;
    @Mock
    private TokenBlacklistService tokenBlacklistService;
    @Mock
    private SmsCodeService smsCodeService;

    @InjectMocks
    private AuthServiceImpl authService;

    @Test
    void refresh_reusedToken_shouldReturn10504() {
        AuthRefreshTokenEntity entity = new AuthRefreshTokenEntity();
        entity.setId(1L);
        entity.setUserId(10001L);
        entity.setDeviceId("dev-device-001");
        entity.setPlatform(3);
        entity.setUsedAt(Instant.now());

        when(authRefreshTokenManager.validateAndConsume(anyString(), anyString(), anyInt()))
                .thenThrow(new GatewayBizException(GatewayErrorCode.REFRESH_TOKEN_REUSED));

        RefreshTokenReq req = new RefreshTokenReq();
        req.setRefreshToken("old-refresh");
        req.setDeviceId("dev-device-001");
        req.setPlatform(3);

        GatewayBizException ex = assertThrows(GatewayBizException.class, () -> authService.refresh(req));
        assertEquals(GatewayErrorCode.REFRESH_TOKEN_REUSED, ex.getErrorCode());
        verify(jwtIssuer, never()).issueTokenPair(anyLong(), anyString(), anyInt(), any());
    }

    @Test
    void refresh_success_shouldRotateRefreshToken() {
        AuthRefreshTokenEntity entity = new AuthRefreshTokenEntity();
        entity.setId(10L);
        entity.setUserId(10001L);
        entity.setDeviceId("dev-device-001");
        entity.setPlatform(3);

        when(authRefreshTokenManager.validateAndConsume("old-refresh", "dev-device-001", 3))
                .thenReturn(entity);
        when(jwtIssuer.issueTokenPair(10001L, "dev-device-001", 3, null))
                .thenReturn(new TokenPair("access", "new-refresh", "access-jti", "refresh-jti", 1000L, 2000L));

        RefreshTokenReq req = new RefreshTokenReq();
        req.setRefreshToken("old-refresh");
        req.setDeviceId("dev-device-001");
        req.setPlatform(3);

        authService.refresh(req);

        verify(authRefreshTokenManager).insertRefreshToken(eq(10001L), eq("dev-device-001"), eq(3),
                eq("new-refresh"), eq("refresh-jti"), any(Instant.class));
        verify(authRefreshTokenManager).markReplaced(10L, "refresh-jti");
    }
}
