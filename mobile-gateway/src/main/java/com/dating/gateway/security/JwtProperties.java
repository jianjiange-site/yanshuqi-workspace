package com.dating.gateway.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * JWT 签发与验签配置。
 */
@ConfigurationProperties(prefix = "gateway.jwt")
public class JwtProperties {

    private String issuer = "dating-mobile-gateway";
    private long accessTokenTtlSeconds = 900L;
    private long refreshTokenTtlSeconds = 604800L;
    /** Base64 编码的 PKCS#8 RSA 私钥；dev/test 可留空以自动生成测试密钥。 */
    private String privateKeyBase64 = "";
    /** Base64 编码的 X.509 RSA 公钥；dev/test 可留空以自动生成测试密钥。 */
    private String publicKeyBase64 = "";

    public String getIssuer() {
        return issuer;
    }

    public void setIssuer(String issuer) {
        this.issuer = issuer;
    }

    public long getAccessTokenTtlSeconds() {
        return accessTokenTtlSeconds;
    }

    public void setAccessTokenTtlSeconds(long accessTokenTtlSeconds) {
        this.accessTokenTtlSeconds = accessTokenTtlSeconds;
    }

    public long getRefreshTokenTtlSeconds() {
        return refreshTokenTtlSeconds;
    }

    public void setRefreshTokenTtlSeconds(long refreshTokenTtlSeconds) {
        this.refreshTokenTtlSeconds = refreshTokenTtlSeconds;
    }

    public String getPrivateKeyBase64() {
        return privateKeyBase64;
    }

    public void setPrivateKeyBase64(String privateKeyBase64) {
        this.privateKeyBase64 = privateKeyBase64;
    }

    public String getPublicKeyBase64() {
        return publicKeyBase64;
    }

    public void setPublicKeyBase64(String publicKeyBase64) {
        this.publicKeyBase64 = publicKeyBase64;
    }
}
