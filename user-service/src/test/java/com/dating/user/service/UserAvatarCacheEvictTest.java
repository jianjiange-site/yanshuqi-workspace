package com.dating.user.service;

import com.dating.user.config.AvatarUploadProperties;
import com.dating.user.config.ObjectStorageProperties;
import com.dating.user.constant.AccountStatus;
import com.dating.user.constant.PhotoType;
import com.dating.user.constant.ProfileStatus;
import com.dating.user.constant.UserType;
import com.dating.user.dto.ConfirmAvatarUploadCommand;
import com.dating.user.entity.UserEntity;
import com.dating.user.entity.UserPhotoEntity;
import com.dating.user.entity.UserProfileEntity;
import com.dating.user.manager.UserManager;
import com.dating.user.manager.UserPhotoManager;
import com.dating.user.manager.UserProfileManager;
import com.dating.user.service.impl.UserAvatarServiceImpl;
import com.dating.user.service.impl.UserPhotoServiceImpl;
import com.dating.user.service.storage.MockObjectStorageService;
import com.dating.user.service.support.AvatarObjectKeyGenerator;
import com.dating.user.service.support.AvatarUploadValidator;
import com.dating.user.service.support.AvatarViewConverter;
import com.dating.user.service.support.BusinessIdGenerator;
import com.dating.user.service.support.PhotoObjectKeyValidator;
import com.dating.user.service.support.ProfileStatusResolver;
import com.dating.user.service.support.SlowCallLogger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserAvatarCacheEvictTest {

    private static final long USER_ID = 8003L;

    private static final String OBJECT_KEY = "avatar/" + USER_ID + "/202606/cache-test.jpg";

    @Mock
    private UserManager userManager;
    @Mock
    private UserProfileManager userProfileManager;
    @Mock
    private UserPhotoManager userPhotoManager;
    @Mock
    private UserCacheInvalidationService userCacheInvalidationService;
    @Mock
    private BusinessIdGenerator businessIdGenerator;

    private UserAvatarService userAvatarService;

    @BeforeEach
    void setUp() {
        AvatarUploadProperties uploadProperties = new AvatarUploadProperties();
        ObjectStorageProperties storageProperties = new ObjectStorageProperties();
        MockObjectStorageService objectStorageService = new MockObjectStorageService(storageProperties);
        objectStorageService.registerForTest(OBJECT_KEY, 1024L, "image/png", 0, 0);

        PhotoObjectKeyValidator validator = new PhotoObjectKeyValidator();
        UserPhotoService userPhotoService = new UserPhotoServiceImpl(
                userManager, userProfileManager, userPhotoManager, validator,
                new ProfileStatusResolver(), businessIdGenerator, userCacheInvalidationService,
                SlowCallLogger.forTest());

        userAvatarService = new UserAvatarServiceImpl(
                userManager,
                new AvatarUploadValidator(uploadProperties, validator),
                new AvatarObjectKeyGenerator(uploadProperties),
                uploadProperties,
                objectStorageService,
                userPhotoService,
                new AvatarViewConverter(),
                SlowCallLogger.forTest());
    }

    @Test
    void confirmShouldEvictProfileCache() {
        UserEntity user = new UserEntity();
        user.setUserId(USER_ID);
        user.setUserType(UserType.BH.name());
        user.setAccountStatus(AccountStatus.ACTIVE.name());
        user.setProfileStatus(ProfileStatus.BASIC_DONE.name());
        when(userManager.findByUserId(USER_ID)).thenReturn(user);

        UserProfileEntity profile = new UserProfileEntity();
        profile.setUserId(USER_ID);
        profile.setProfileCompleted(1);
        when(userProfileManager.findByUserId(USER_ID)).thenReturn(profile);
        when(userPhotoManager.findByUserIdAndObjectKey(USER_ID, OBJECT_KEY)).thenReturn(null, photo());

        when(businessIdGenerator.nextId()).thenReturn(88001L);

        ConfirmAvatarUploadCommand command = new ConfirmAvatarUploadCommand();
        command.setUserId(USER_ID);
        command.setObjectKey(OBJECT_KEY);
        userAvatarService.confirmAvatarUpload(command);

        verify(userCacheInvalidationService).evictProfileCache(USER_ID);
    }

    private UserPhotoEntity photo() {
        UserPhotoEntity entity = new UserPhotoEntity();
        entity.setPhotoId(88001L);
        entity.setUserId(USER_ID);
        entity.setPhotoType(PhotoType.AVATAR.name());
        entity.setObjectKey(OBJECT_KEY);
        entity.setEnabled(1);
        return entity;
    }
}
