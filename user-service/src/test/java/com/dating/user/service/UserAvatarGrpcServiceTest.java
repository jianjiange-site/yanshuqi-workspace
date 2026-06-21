package com.dating.user.service;

import com.dating.user.config.AvatarUploadProperties;
import com.dating.user.config.ObjectStorageProperties;
import com.dating.user.constant.AccountStatus;
import com.dating.user.constant.UserType;
import com.dating.user.dto.PresignAvatarUploadCommand;
import com.dating.user.entity.UserEntity;
import com.dating.user.manager.UserManager;
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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserAvatarGrpcServiceTest {

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
    void serviceLayerShouldReturnPresignFieldsForGrpcMapping() {
        UserEntity user = new UserEntity();
        user.setUserId(9001L);
        user.setUserType(UserType.BH.name());
        user.setAccountStatus(AccountStatus.ACTIVE.name());
        when(userManager.findByUserId(9001L)).thenReturn(user);

        PresignAvatarUploadCommand command = new PresignAvatarUploadCommand();
        command.setUserId(9001L);
        command.setExt("webp");
        command.setExpectedSizeBytes(4096L);

        PresignAvatarUploadResult result = userAvatarService.presignAvatarUpload(command);
        assertNotNull(result.getPresignedUrl());
        assertNotNull(result.getObjectKey());
        assertFalse(result.getPresignedUrl().isBlank());
    }
}
