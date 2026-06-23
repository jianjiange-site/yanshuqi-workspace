package com.dating.gateway.security;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Date;
import java.util.UUID;

/**
 * RS256 access token 签发器。
 */
@Component
public class JwtIssuer {

    private final JwtProperties jwtProperties;
    private final JwtKeyProvider jwtKeyProvider;

    public JwtIssuer(JwtProperties jwtProperties, JwtKeyProvider jwtKeyProvider) {
        this.jwtProperties = jwtProperties;
        this.jwtKeyProvider = jwtKeyProvider;
    }

    /**
     * 签发 access token，claims 含 sub/jti/deviceId/platform/tokenVersion。
     */
    public TokenPair issueTokenPair(long userId,
                                    String deviceId,
                                    int platform,
                                    Integer tokenVersion) {
        Instant now = Instant.now();
        Instant accessExpires = now.plusSeconds(jwtProperties.getAccessTokenTtlSeconds());
        Instant refreshExpires = now.plusSeconds(jwtProperties.getRefreshTokenTtlSeconds());

        String accessJti = UUID.randomUUID().toString().replace("-", "");
        String refreshJti = UUID.randomUUID().toString().replace("-", "");
        String refreshTokenPlain = UUID.randomUUID().toString().replace("-", "")
                + UUID.randomUUID().toString().replace("-", "");

        String accessToken = buildAccessToken(userId, deviceId, platform, tokenVersion, accessJti, now, accessExpires);

        return new TokenPair(
                accessToken,
                refreshTokenPlain,
                accessJti,
                refreshJti,
                accessExpires.toEpochMilli(),
                refreshExpires.toEpochMilli());
    }

    private String buildAccessToken(long userId,
                                    String deviceId,
                                    int platform,
                                    Integer tokenVersion,
                                    String jti,
                                    Instant issuedAt,
                                    Instant expiresAt) {
        JWTClaimsSet.Builder builder = new JWTClaimsSet.Builder()
                .issuer(jwtProperties.getIssuer())
                .subject(String.valueOf(userId))
                .jwtID(jti)
                .issueTime(Date.from(issuedAt))
                .expirationTime(Date.from(expiresAt))
                .claim("deviceId", deviceId)
                .claim("platform", platform);
        if (tokenVersion != null) {
            builder.claim("tokenVersion", tokenVersion);
        }
        SignedJWT signedJwt = new SignedJWT(
                new JWSHeader.Builder(JWSAlgorithm.RS256).build(),
                builder.build());
        try {
            signedJwt.sign(new RSASSASigner(jwtKeyProvider.getPrivateKey()));
            return signedJwt.serialize();
        } catch (Exception ex) {
            throw new IllegalStateException("签发 access token 失败", ex);
        }
    }
}
