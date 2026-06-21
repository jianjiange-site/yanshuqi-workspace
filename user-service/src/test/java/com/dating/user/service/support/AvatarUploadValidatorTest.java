package com.dating.user.service.support;

import com.dating.user.config.AvatarUploadProperties;
import com.dating.user.exception.UserBizException;
import com.dating.user.exception.UserErrorCode;
import com.dating.user.service.storage.ObjectStat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AvatarUploadValidatorTest {

    private AvatarUploadValidator validator;

    @BeforeEach
    void setUp() {
        AvatarUploadProperties properties = new AvatarUploadProperties();
        validator = new AvatarUploadValidator(properties, new PhotoObjectKeyValidator());
    }

    @Test
    void shouldAcceptAllowedExtCaseInsensitive() {
        assertEquals("jpg", validator.normalizeExt("JPG"));
        assertEquals("jpeg", validator.normalizeExt("jpeg"));
        assertEquals("png", validator.normalizeExt("png"));
        assertEquals("webp", validator.normalizeExt("webp"));
    }

    @Test
    void shouldRejectGifExt() {
        UserBizException ex = assertThrows(UserBizException.class, () -> validator.normalizeExt("gif"));
        assertEquals(UserErrorCode.INVALID_AVATAR_EXT, ex.getErrorCode());
    }

    @Test
    void shouldRejectZeroExpectedSize() {
        UserBizException ex = assertThrows(UserBizException.class,
                () -> validator.validatePresign(1L, "jpg", 0L));
        assertEquals(UserErrorCode.AVATAR_SIZE_EXCEEDED, ex.getErrorCode());
    }

    @Test
    void shouldRejectOversizeExpectedSize() {
        UserBizException ex = assertThrows(UserBizException.class,
                () -> validator.validatePresign(1L, "jpg", 10_485_761L));
        assertEquals(UserErrorCode.AVATAR_SIZE_EXCEEDED, ex.getErrorCode());
    }

    @Test
    void shouldRejectObjectKeyNotBelongToUser() {
        long userId = 100L;
        String otherKey = "avatar/200/202606/uuid.jpg";
        UserBizException ex = assertThrows(UserBizException.class,
                () -> validator.validateConfirmObjectKey(userId, otherKey));
        assertEquals(UserErrorCode.AVATAR_OBJECT_NOT_BELONG_TO_USER, ex.getErrorCode());
    }

    @Test
    void shouldRejectPathTraversal() {
        UserBizException ex = assertThrows(UserBizException.class,
                () -> validator.validateConfirmObjectKey(100L, "avatar/100/../200/uuid.jpg"));
        assertEquals(UserErrorCode.INVALID_AVATAR_OBJECT_KEY, ex.getErrorCode());
    }

    @Test
    void shouldRejectOversizeStatObject() {
        UserBizException ex = assertThrows(UserBizException.class,
                () -> validator.validateStatObject(new ObjectStat(10_485_761L, "image/jpeg", 0, 0)));
        assertEquals(UserErrorCode.AVATAR_SIZE_EXCEEDED, ex.getErrorCode());
    }
}
