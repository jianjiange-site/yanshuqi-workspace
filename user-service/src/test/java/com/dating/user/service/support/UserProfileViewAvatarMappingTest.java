package com.dating.user.service.support;

import com.dating.user.vo.AvatarViewVO;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class UserProfileViewAvatarMappingTest {

    private final AvatarViewConverter avatarViewConverter = new AvatarViewConverter();

    @Test
    void shouldMapThreeKeysIdenticallyWithoutThumbnailService() {
        String key = "avatar/100/202606/uuid.jpg";
        AvatarViewVO avatar = avatarViewConverter.fromObjectKey(key, 640, 480);
        assertEquals(key, avatar.getOriginalKey());
        assertEquals(key, avatar.getMinKey());
        assertEquals(key, avatar.getMidKey());
        assertEquals(640, avatar.getWidth());
        assertEquals(480, avatar.getHeight());
    }

    @Test
    void shouldReturnNullForBlankKey() {
        assertNull(avatarViewConverter.fromObjectKey(null, 0, 0));
        assertNull(avatarViewConverter.fromObjectKey("  ", 0, 0));
    }

    @Test
    void shouldDefaultWidthHeightToZero() {
        AvatarViewVO avatar = avatarViewConverter.fromObjectKey("avatar/1/202606/a.webp", 0, 0);
        assertEquals(0, avatar.getWidth());
        assertEquals(0, avatar.getHeight());
    }
}
