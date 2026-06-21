package com.dating.user.service.support;

import com.dating.user.config.AvatarUploadProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AvatarObjectKeyGeneratorTest {

    private AvatarObjectKeyGenerator generator;

    @BeforeEach
    void setUp() {
        AvatarUploadProperties properties = new AvatarUploadProperties();
        properties.setObjectKeyPrefix("avatar");
        generator = new AvatarObjectKeyGenerator(properties);
    }

    @Test
    void shouldGenerateKeyWithUserIdPrefixAndExt() {
        String key = generator.generate(326919587948007424L, "jpg");
        assertTrue(key.startsWith("avatar/326919587948007424/"));
        assertTrue(key.endsWith(".jpg"));
        assertEquals(4, key.split("/").length);
    }

    @Test
    void shouldUseLowercaseExt() {
        String key = generator.generate(100L, "PNG");
        assertTrue(key.endsWith(".png"));
    }
}
