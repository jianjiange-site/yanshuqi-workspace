package com.dating.gateway.security;

import com.dating.gateway.exception.GatewayBizException;
import com.dating.gateway.exception.GatewayErrorCode;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jwt.SignedJWT;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.time.Instant;
import java.util.Date;

/**
 * RS256 access token 验签与 claims 解析。
 */
@Component
public class JwtVerifier {

    private final JwtProperties jwtProperties;
    private final JwtKeyProvider jwtKeyProvider;

    public JwtVerifier(JwtProperties jwtProperties, JwtKeyProvider jwtKeyProvider) {
        this.jwtProperties = jwtProperties;
        this.jwtKeyProvider = jwtKeyProvider;
    }

    public JwtClaims verifyAccessToken(String token) {
        try {
            SignedJWT signedJwt = SignedJWT.parse(token);
            if (!signedJwt.verify(new RSASSAVerifier(jwtKeyProvider.getPublicKey()))) {
                throw new GatewayBizException(GatewayErrorCode.TOKEN_INVALID);
            }
            var claims = signedJwt.getJWTClaimsSet();
            if (!jwtProperties.getIssuer().equals(claims.getIssuer())) {
                throw new GatewayBizException(GatewayErrorCode.TOKEN_INVALID);
            }
            Date expiration = claims.getExpirationTime();
            if (expiration == null || expiration.toInstant().isBefore(Instant.now())) {
                throw new GatewayBizException(GatewayErrorCode.TOKEN_EXPIRED);
            }
            long userId = Long.parseLong(claims.getSubject());
            String jti = claims.getJWTID();
            String deviceId = claims.getStringClaim("deviceId");
            Number platformNumber = claims.getIntegerClaim("platform");
            if (platformNumber == null) {
                platformNumber = claims.getLongClaim("platform");
            }
            Integer tokenVersion = claims.getIntegerClaim("tokenVersion");
            if (userId <= 0 || jti == null || jti.isBlank() || deviceId == null || deviceId.isBlank()
                    || platformNumber == null) {
                throw new GatewayBizException(GatewayErrorCode.TOKEN_INVALID);
            }
            return new JwtClaims(
                    userId,
                    jti,
                    deviceId,
                    platformNumber.intValue(),
                    tokenVersion,
                    expiration.toInstant().getEpochSecond());
        } catch (GatewayBizException ex) {
            throw ex;
        } catch (ParseException ex) {
            throw new GatewayBizException(GatewayErrorCode.TOKEN_INVALID);
        } catch (Exception ex) {
            throw new GatewayBizException(GatewayErrorCode.TOKEN_INVALID);
        }
    }
}
