package com.dating.gateway.security;

import com.nimbusds.jose.jwk.gen.RSAKeyGenerator;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Arrays;
import java.util.Base64;

/**
 * 加载或生成 RS256 密钥对；prod 必须显式配置，dev/test 可自动生成测试密钥。
 */
@Component
public class JwtKeyProvider {

    private static final Logger log = LoggerFactory.getLogger(JwtKeyProvider.class);

    private final JwtProperties jwtProperties;
    private final Environment environment;

    private RSAPrivateKey privateKey;
    private RSAPublicKey publicKey;
    private boolean ephemeralKeys;

    public JwtKeyProvider(JwtProperties jwtProperties, Environment environment) {
        this.jwtProperties = jwtProperties;
        this.environment = environment;
    }

    @PostConstruct
    public void init() {
        if (StringUtils.hasText(jwtProperties.getPrivateKeyBase64())
                && StringUtils.hasText(jwtProperties.getPublicKeyBase64())) {
            privateKey = loadPrivateKey(jwtProperties.getPrivateKeyBase64());
            publicKey = loadPublicKey(jwtProperties.getPublicKeyBase64());
            ephemeralKeys = false;
            return;
        }
        if (isDevOrTestProfile()) {
            generateEphemeralKeyPair();
            ephemeralKeys = true;
            log.warn("JWT 使用自动生成的 ephemeral 测试密钥，仅限 dev/test，禁止用于生产");
            return;
        }
        throw new IllegalStateException("生产环境必须配置 gateway.jwt.private-key-base64 与 public-key-base64");
    }

    public RSAPrivateKey getPrivateKey() {
        return privateKey;
    }

    public RSAPublicKey getPublicKey() {
        return publicKey;
    }

    public boolean isEphemeralKeys() {
        return ephemeralKeys;
    }

    private boolean isDevOrTestProfile() {
        return Arrays.stream(environment.getActiveProfiles())
                .anyMatch(profile -> "dev".equals(profile) || "test".equals(profile))
                || environment.getActiveProfiles().length == 0;
    }

    private void generateEphemeralKeyPair() {
        try {
            var rsaKey = new RSAKeyGenerator(2048).generate();
            privateKey = rsaKey.toRSAPrivateKey();
            publicKey = rsaKey.toRSAPublicKey();
        } catch (Exception ex) {
            throw new IllegalStateException("生成测试 JWT 密钥失败", ex);
        }
    }

    private RSAPrivateKey loadPrivateKey(String base64) {
        try {
            byte[] encoded = Base64.getDecoder().decode(base64);
            var keyFactory = java.security.KeyFactory.getInstance("RSA");
            var spec = new java.security.spec.PKCS8EncodedKeySpec(encoded);
            return (RSAPrivateKey) keyFactory.generatePrivate(spec);
        } catch (Exception ex) {
            throw new IllegalStateException("加载 JWT 私钥失败", ex);
        }
    }

    private RSAPublicKey loadPublicKey(String base64) {
        try {
            byte[] encoded = Base64.getDecoder().decode(base64);
            var keyFactory = java.security.KeyFactory.getInstance("RSA");
            var spec = new java.security.spec.X509EncodedKeySpec(encoded);
            return (RSAPublicKey) keyFactory.generatePublic(spec);
        } catch (Exception ex) {
            throw new IllegalStateException("加载 JWT 公钥失败", ex);
        }
    }
}
