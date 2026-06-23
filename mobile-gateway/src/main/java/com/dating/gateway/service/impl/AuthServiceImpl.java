package com.dating.gateway.service.impl;

import com.dating.gateway.client.UserAuthGrpcClient;
import com.dating.gateway.dto.LoginDeviceReq;
import com.dating.gateway.dto.LoginPhoneReq;
import com.dating.gateway.dto.LoginThirdPartyReq;
import com.dating.gateway.dto.RefreshTokenReq;
import com.dating.gateway.dto.vo.LoginResultVO;
import com.dating.gateway.dto.vo.SendSmsCodeVO;
import com.dating.gateway.entity.AuthRefreshTokenEntity;
import com.dating.gateway.exception.GatewayBizException;
import com.dating.gateway.exception.GatewayErrorCode;
import com.dating.gateway.manager.AuthDeviceManager;
import com.dating.gateway.manager.AuthRefreshTokenManager;
import com.dating.gateway.security.CurrentUserContext;
import com.dating.gateway.security.JwtClaims;
import com.dating.gateway.security.JwtIssuer;
import com.dating.gateway.security.PlatformCodec;
import com.dating.gateway.security.TokenBlacklistService;
import com.dating.gateway.security.TokenPair;
import com.dating.gateway.service.AuthService;
import com.dating.gateway.service.SmsCodeService;
import com.dating.user.grpc.proto.ResolveOrCreateDeviceUserRequest;
import com.dating.user.grpc.proto.ResolveOrCreateLoginUserResponse;
import com.dating.user.grpc.proto.ResolveOrCreatePhoneUserRequest;
import com.dating.user.grpc.proto.ResolveOrCreateThirdPartyUserRequest;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Instant;

/**
 * 登录编排：调用 user-service 解析/创建用户 → 登记设备 → 签发 JWT → 落 refresh hash。
 */
@Service
public class AuthServiceImpl implements AuthService {

    private final UserAuthGrpcClient userAuthGrpcClient;
    private final AuthDeviceManager authDeviceManager;
    private final AuthRefreshTokenManager authRefreshTokenManager;
    private final JwtIssuer jwtIssuer;
    private final TokenBlacklistService tokenBlacklistService;
    private final SmsCodeService smsCodeService;

    public AuthServiceImpl(UserAuthGrpcClient userAuthGrpcClient,
                           AuthDeviceManager authDeviceManager,
                           AuthRefreshTokenManager authRefreshTokenManager,
                           JwtIssuer jwtIssuer,
                           TokenBlacklistService tokenBlacklistService,
                           SmsCodeService smsCodeService) {
        this.userAuthGrpcClient = userAuthGrpcClient;
        this.authDeviceManager = authDeviceManager;
        this.authRefreshTokenManager = authRefreshTokenManager;
        this.jwtIssuer = jwtIssuer;
        this.tokenBlacklistService = tokenBlacklistService;
        this.smsCodeService = smsCodeService;
    }

    @Override
    public SendSmsCodeVO sendSmsCode(String phone) {
        String mockCode = smsCodeService.sendCode(phone);
        SendSmsCodeVO vo = new SendSmsCodeVO();
        vo.setMockCode(mockCode);
        return vo;
    }

    @Override
    public LoginResultVO loginDevice(LoginDeviceReq req) {
        int platform = PlatformCodec.parsePlatformCode(req.getPlatform());
        ResolveOrCreateDeviceUserRequest grpcRequest = ResolveOrCreateDeviceUserRequest.newBuilder()
                .setDeviceId(req.getDeviceId().trim())
                .setPlatform(PlatformCodec.toUserServicePlatform(platform))
                .setDeviceModel(nullToEmpty(req.getDeviceModel()))
                .setOsVersion(nullToEmpty(req.getOsVersion()))
                .setAppVersion(nullToEmpty(req.getAppVersion()))
                .setPushToken(nullToEmpty(req.getPushToken()))
                .build();
        ResolveOrCreateLoginUserResponse userResponse = userAuthGrpcClient.resolveOrCreateDeviceUser(grpcRequest);
        return completeLogin(userResponse, req.getDeviceId().trim(), platform,
                req.getDeviceModel(), req.getOsVersion(), req.getAppVersion(), req.getPushToken());
    }

    @Override
    public LoginResultVO loginPhone(LoginPhoneReq req) {
        smsCodeService.verifyAndConsume(req.getPhone(), req.getSmsCode());
        int platform = PlatformCodec.parsePlatformCode(req.getPlatform());
        ResolveOrCreatePhoneUserRequest grpcRequest = ResolveOrCreatePhoneUserRequest.newBuilder()
                .setPhone(req.getPhone().trim())
                .setSmsCode(req.getSmsCode())
                .setDeviceId(req.getDeviceId().trim())
                .setPlatform(PlatformCodec.toUserServicePlatform(platform))
                .setDeviceModel(nullToEmpty(req.getDeviceModel()))
                .setOsVersion(nullToEmpty(req.getOsVersion()))
                .setAppVersion(nullToEmpty(req.getAppVersion()))
                .setPushToken(nullToEmpty(req.getPushToken()))
                .build();
        ResolveOrCreateLoginUserResponse userResponse = userAuthGrpcClient.resolveOrCreatePhoneUser(grpcRequest);
        return completeLogin(userResponse, req.getDeviceId().trim(), platform,
                req.getDeviceModel(), req.getOsVersion(), req.getAppVersion(), req.getPushToken());
    }

    @Override
    public LoginResultVO loginThirdParty(LoginThirdPartyReq req) {
        validateThirdPartyPlatform(req.getThirdPartyPlatform());
        int platform = PlatformCodec.parsePlatformCode(req.getPlatform());
        ResolveOrCreateThirdPartyUserRequest grpcRequest = ResolveOrCreateThirdPartyUserRequest.newBuilder()
                .setThirdPartyPlatform(req.getThirdPartyPlatform())
                .setIdToken(req.getIdToken())
                .setGoogleEmail(nullToEmpty(req.getGoogleEmail()))
                .setDeviceId(req.getDeviceId().trim())
                .setPlatform(PlatformCodec.toUserServicePlatform(platform))
                .setDeviceModel(nullToEmpty(req.getDeviceModel()))
                .setOsVersion(nullToEmpty(req.getOsVersion()))
                .setAppVersion(nullToEmpty(req.getAppVersion()))
                .setPushToken(nullToEmpty(req.getPushToken()))
                .build();
        ResolveOrCreateLoginUserResponse userResponse = userAuthGrpcClient.resolveOrCreateThirdPartyUser(grpcRequest);
        return completeLogin(userResponse, req.getDeviceId().trim(), platform,
                req.getDeviceModel(), req.getOsVersion(), req.getAppVersion(), req.getPushToken());
    }

    @Override
    public LoginResultVO refresh(RefreshTokenReq req) {
        if (req.getPlatform() == null) {
            throw new GatewayBizException(GatewayErrorCode.INVALID_ARGUMENT, "platform 不能为空");
        }
        int platform = PlatformCodec.parsePlatformCode(req.getPlatform());
        AuthRefreshTokenEntity consumed = authRefreshTokenManager.validateAndConsume(
                req.getRefreshToken(), req.getDeviceId().trim(), platform);

        TokenPair tokenPair = jwtIssuer.issueTokenPair(
                consumed.getUserId(),
                consumed.getDeviceId(),
                consumed.getPlatform(),
                null);
        Instant refreshExpires = Instant.ofEpochMilli(tokenPair.getRefreshExpiresAtMs());
        authRefreshTokenManager.insertRefreshToken(
                consumed.getUserId(),
                consumed.getDeviceId(),
                consumed.getPlatform(),
                tokenPair.getRefreshToken(),
                tokenPair.getRefreshJti(),
                refreshExpires);
        authRefreshTokenManager.markReplaced(consumed.getId(), tokenPair.getRefreshJti());

        LoginResultVO vo = new LoginResultVO();
        vo.setUserId(consumed.getUserId());
        vo.setAccessToken(tokenPair.getAccessToken());
        vo.setRefreshToken(tokenPair.getRefreshToken());
        vo.setAccessExpiresAtMs(tokenPair.getAccessExpiresAtMs());
        vo.setRefreshExpiresAtMs(tokenPair.getRefreshExpiresAtMs());
        vo.setPending(null);
        vo.setNewlyCreated(null);
        return vo;
    }

    @Override
    public void logout() {
        JwtClaims claims = CurrentUserContext.get();
        if (claims == null) {
            throw new GatewayBizException(GatewayErrorCode.TOKEN_INVALID);
        }
        Instant expiresAt = Instant.ofEpochSecond(claims.getExpiresAtEpochSeconds());
        tokenBlacklistService.blacklist(claims.getJti(), expiresAt);
        authRefreshTokenManager.revokeByUserAndDevice(
                claims.getUserId(), claims.getDeviceId(), claims.getPlatform());
    }

    /**
     * 登录公共收尾：设备 upsert + token 签发 + refresh 落库。
     */
    private LoginResultVO completeLogin(ResolveOrCreateLoginUserResponse userResponse,
                                        String deviceId,
                                        int platform,
                                        String deviceModel,
                                        String osVersion,
                                        String appVersion,
                                        String pushToken) {
        Instant now = Instant.now();
        authDeviceManager.upsertDevice(
                userResponse.getUserId(),
                deviceId,
                platform,
                deviceModel,
                osVersion,
                appVersion,
                pushToken,
                now);

        Integer tokenVersion = userResponse.getTokenVersion() == 0 ? null : userResponse.getTokenVersion();
        TokenPair tokenPair = jwtIssuer.issueTokenPair(
                userResponse.getUserId(), deviceId, platform, tokenVersion);

        authRefreshTokenManager.insertRefreshToken(
                userResponse.getUserId(),
                deviceId,
                platform,
                tokenPair.getRefreshToken(),
                tokenPair.getRefreshJti(),
                Instant.ofEpochMilli(tokenPair.getRefreshExpiresAtMs()));

        LoginResultVO vo = new LoginResultVO();
        vo.setUserId(userResponse.getUserId());
        vo.setPending(userResponse.getPending());
        vo.setNewlyCreated(userResponse.getNewlyCreated());
        vo.setAccessToken(tokenPair.getAccessToken());
        vo.setRefreshToken(tokenPair.getRefreshToken());
        vo.setAccessExpiresAtMs(tokenPair.getAccessExpiresAtMs());
        vo.setRefreshExpiresAtMs(tokenPair.getRefreshExpiresAtMs());
        return vo;
    }

    private void validateThirdPartyPlatform(Integer platform) {
        if (platform == null || platform < 1 || platform > 3) {
            throw new GatewayBizException(GatewayErrorCode.THIRD_PARTY_TOKEN_INVALID);
        }
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }
}
