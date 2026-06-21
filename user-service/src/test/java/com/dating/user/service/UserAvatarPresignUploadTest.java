package com.dating.user.service;

import com.dating.user.config.AvatarUploadProperties;
import com.dating.user.config.ObjectStorageProperties;
import com.dating.user.constant.AccountStatus;
import com.dating.user.constant.UserType;
import com.dating.user.dto.PresignAvatarUploadCommand;
import com.dating.user.entity.UserEntity;
import com.dating.user.exception.UserBizException;
import com.dating.user.exception.UserErrorCode;
import com.dating.user.manager.UserManager;
import com.dating.user.manager.UserPhotoManager;
import com.dating.user.manager.UserProfileManager;
import com.dating.user.service.impl.UserAvatarServiceImpl;
import com.dating.user.service.storage.MockObjectStorageService;
import com.dating.user.service.support.AvatarObjectKeyGenerator;
import com.dating.user.service.support.AvatarUploadValidator;
import com.dating.user.service.support.AvatarViewConverter;
import com.dating.user.service.support.PhotoObjectKeyValidator;
import com.dating.user.service.support.SlowCallLogger;
import com.dating.user.vo.PresignAvatarUploadResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserAvatarPresignUploadTest {

    private static final long USER_ID = 8001L;

    @Mock
    private UserManager userManager;
    @Mock
    private UserPhotoService userPhotoService;

    private UserAvatarService userAvatarService;

    @BeforeEach
    void setUp() {
        AvatarUploadProperties uploadProperties = new AvatarUploadProperties();
        ObjectStorageProperties storageProperties = new ObjectStorageProperties();
        userAvatarService = new UserAvatarServiceImpl(
                userManager,
                new AvatarUploadValidator(uploadProperties, new PhotoObjectKeyValidator()),
                new AvatarObjectKeyGenerator(uploadProperties),
                uploadProperties,
                new MockObjectStorageService(storageProperties),
                userPhotoService,
                new AvatarViewConverter(),
                SlowCallLogger.forTest());
    }

    @Test
    void shouldPresignJpgSuccessfully() {
        when(userManager.findByUserId(USER_ID)).thenReturn(activeUser());
        PresignAvatarUploadCommand command = new PresignAvatarUploadCommand();
        command.setUserId(USER_ID);
        command.setExt("jpg");
        command.setExpectedSizeBytes(1024L);
        PresignAvatarUploadResult result = userAvatarService.presignAvatarUpload(command);
        org.junit.jupiter.api.Assertions.assertTrue(result.getObjectKey().endsWith(".jpg"));
    }

    @Test
    void shouldRejectBannedUser() {
        UserEntity user = activeUser();
        user.setAccountStatus(AccountStatus.BANNED.name());
        when(userManager.findByUserId(USER_ID)).thenReturn(user);
        PresignAvatarUploadCommand command = new PresignAvatarUploadCommand();
        command.setUserId(USER_ID);
        command.setExt("png");
        command.setExpectedSizeBytes(2048L);
        UserBizException ex = assertThrows(UserBizException.class, () -> userAvatarService.presignAvatarUpload(command));
        assertEquals(UserErrorCode.USER_BANNED, ex.getErrorCode());
    }

    @Test
    void shouldRejectUserNotFound() {
        when(userManager.findByUserId(USER_ID)).thenReturn(null);
        PresignAvatarUploadCommand command = new PresignAvatarUploadCommand();
        command.setUserId(USER_ID);
        command.setExt("jpg");
        command.setExpectedSizeBytes(100L);
        UserBizException ex = assertThrows(UserBizException.class, () -> userAvatarService.presignAvatarUpload(command));
        assertEquals(UserErrorCode.USER_NOT_FOUND, ex.getErrorCode());
    }

    private UserEntity activeUser() {
        UserEntity entity = new UserEntity();
        entity.setUserId(USER_ID);
        entity.setUserType(UserType.BH.name());
        entity.setAccountStatus(AccountStatus.ACTIVE.name());
        return entity;
    }
}
