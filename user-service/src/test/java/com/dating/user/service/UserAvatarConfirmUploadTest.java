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
import com.dating.user.exception.UserBizException;
import com.dating.user.exception.UserErrorCode;
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
import com.dating.user.vo.AvatarViewVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserAvatarConfirmUploadTest {

    private static final long USER_ID = 8002L;

    private static final String OBJECT_KEY = "avatar/" + USER_ID + "/202606/test-uuid.jpg";

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

    private MockObjectStorageService objectStorageService;
    private UserAvatarService userAvatarService;

    @BeforeEach
    void setUp() {
        AvatarUploadProperties uploadProperties = new AvatarUploadProperties();
        ObjectStorageProperties storageProperties = new ObjectStorageProperties();
        objectStorageService = new MockObjectStorageService(storageProperties);
        objectStorageService.registerForTest(OBJECT_KEY, 2048L, "image/jpeg", 800, 600);

        PhotoObjectKeyValidator photoObjectKeyValidator = new PhotoObjectKeyValidator();
        UserPhotoService userPhotoService = new UserPhotoServiceImpl(
                userManager, userProfileManager, userPhotoManager, photoObjectKeyValidator,
                new ProfileStatusResolver(), businessIdGenerator, userCacheInvalidationService,
                SlowCallLogger.forTest());

        userAvatarService = new UserAvatarServiceImpl(
                userManager,
                new AvatarUploadValidator(uploadProperties, photoObjectKeyValidator),
                new AvatarObjectKeyGenerator(uploadProperties),
                uploadProperties,
                objectStorageService,
                userPhotoService,
                new AvatarViewConverter(),
                SlowCallLogger.forTest());
    }

    @Test
    void shouldConfirmSuccessfully() {
        stubActiveUserAndProfile();
        when(userPhotoManager.findByUserIdAndObjectKey(USER_ID, OBJECT_KEY)).thenReturn(null, createdPhoto());
        when(businessIdGenerator.nextId()).thenReturn(99001L);

        ConfirmAvatarUploadCommand command = new ConfirmAvatarUploadCommand();
        command.setUserId(USER_ID);
        command.setObjectKey(OBJECT_KEY);

        AvatarViewVO avatar = userAvatarService.confirmAvatarUpload(command);

        assertNotNull(avatar);
        assertEquals(OBJECT_KEY, avatar.getOriginalKey());
        assertEquals(OBJECT_KEY, avatar.getMinKey());
        assertEquals(OBJECT_KEY, avatar.getMidKey());
        assertEquals(800, avatar.getWidth());
        assertEquals(600, avatar.getHeight());
        verify(userProfileManager).updateAvatarKey(USER_ID, OBJECT_KEY);
        verify(userCacheInvalidationService).evictProfileCache(USER_ID);
    }

    @Test
    void shouldRejectObjectKeyNotBelongToUser() {
        when(userManager.findByUserId(USER_ID)).thenReturn(activeUser());
        ConfirmAvatarUploadCommand command = new ConfirmAvatarUploadCommand();
        command.setUserId(USER_ID);
        command.setObjectKey("avatar/999/202606/x.jpg");
        UserBizException ex = assertThrows(UserBizException.class, () -> userAvatarService.confirmAvatarUpload(command));
        assertEquals(UserErrorCode.AVATAR_OBJECT_NOT_BELONG_TO_USER, ex.getErrorCode());
    }

    @Test
    void shouldFailWhenObjectNotFound() {
        when(userManager.findByUserId(USER_ID)).thenReturn(activeUser());
        ConfirmAvatarUploadCommand command = new ConfirmAvatarUploadCommand();
        command.setUserId(USER_ID);
        command.setObjectKey("avatar/" + USER_ID + "/202606/missing.jpg");
        UserBizException ex = assertThrows(UserBizException.class, () -> userAvatarService.confirmAvatarUpload(command));
        assertEquals(UserErrorCode.AVATAR_OBJECT_NOT_FOUND, ex.getErrorCode());
    }

    private void stubActiveUserAndProfile() {
        when(userManager.findByUserId(USER_ID)).thenReturn(activeUser());
        UserProfileEntity profile = new UserProfileEntity();
        profile.setUserId(USER_ID);
        profile.setProfileCompleted(1);
        profile.setAvatarKey("old");
        when(userProfileManager.findByUserId(USER_ID)).thenReturn(profile);
    }

    private UserEntity activeUser() {
        UserEntity entity = new UserEntity();
        entity.setUserId(USER_ID);
        entity.setUserType(UserType.BH.name());
        entity.setAccountStatus(AccountStatus.ACTIVE.name());
        entity.setProfileStatus(ProfileStatus.BASIC_DONE.name());
        return entity;
    }

    private UserPhotoEntity createdPhoto() {
        UserPhotoEntity entity = new UserPhotoEntity();
        entity.setPhotoId(99001L);
        entity.setUserId(USER_ID);
        entity.setPhotoType(PhotoType.AVATAR.name());
        entity.setObjectKey(OBJECT_KEY);
        entity.setEnabled(1);
        return entity;
    }
}
