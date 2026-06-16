package com.dating.user.service;

import com.dating.user.constant.AccountStatus;
import com.dating.user.constant.PhotoType;
import com.dating.user.constant.ProfileStatus;
import com.dating.user.dto.BindPhotoCommand;
import com.dating.user.dto.ListUserPhotosQuery;
import com.dating.user.entity.UserEntity;
import com.dating.user.entity.UserPhotoEntity;
import com.dating.user.entity.UserProfileEntity;
import com.dating.user.exception.UserBizException;
import com.dating.user.exception.UserErrorCode;
import com.dating.user.manager.UserManager;
import com.dating.user.manager.UserPhotoManager;
import com.dating.user.manager.UserProfileManager;
import com.dating.user.service.impl.UserPhotoServiceImpl;
import com.dating.user.service.support.BusinessIdGenerator;
import com.dating.user.service.support.PhotoObjectKeyValidator;
import com.dating.user.service.support.ProfileStatusResolver;
import com.dating.user.vo.BindPhotoResult;
import com.dating.user.vo.UserPhotoVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.lang.reflect.Field;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 用户照片绑定业务单元测试。
 */
@ExtendWith(MockitoExtension.class)
class UserPhotoServiceTest {

    private static final long USER_ID = 325259949544443904L;

    private static final String AVATAR_KEY = "avatar/" + USER_ID + "/202606/a.jpg";

    private static final String ALBUM_KEY = "album/" + USER_ID + "/202606/b.jpg";

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

    private PhotoObjectKeyValidator photoObjectKeyValidator;

    private ProfileStatusResolver profileStatusResolver;

    private UserPhotoService userPhotoService;

    /**
     * 初始化测试依赖。
     */
    @BeforeEach
    void setUp() {
        photoObjectKeyValidator = new PhotoObjectKeyValidator();
        profileStatusResolver = new ProfileStatusResolver();
        userPhotoService = new UserPhotoServiceImpl(
                userManager,
                userProfileManager,
                userPhotoManager,
                photoObjectKeyValidator,
                profileStatusResolver,
                businessIdGenerator,
                userCacheInvalidationService
        );
    }

    @Test
    void bindAvatarShouldSucceed() {
        mockActiveUserAndProfile(1);
        when(userPhotoManager.findByUserIdAndObjectKey(USER_ID, AVATAR_KEY)).thenReturn(null);
        when(businessIdGenerator.nextId()).thenReturn(9001L);
        when(userPhotoManager.findByUserIdAndObjectKey(USER_ID, AVATAR_KEY)).thenReturn(null, buildPhoto(9001L, AVATAR_KEY, PhotoType.AVATAR.name(), 1));

        BindPhotoResult result = userPhotoService.bindUserPhoto(buildBindCommand(PhotoType.AVATAR.name(), AVATAR_KEY, 0));

        assertNotNull(result.getPhoto());
        assertEquals(AVATAR_KEY, result.getAvatarKey());
        verify(userPhotoManager).disableCurrentAvatar(USER_ID);
        verify(userProfileManager).updateAvatarKey(USER_ID, AVATAR_KEY);
    }

    @Test
    void bindAlbumShouldSucceed() {
        mockActiveUserAndProfile(1);
        when(userPhotoManager.findByUserIdAndObjectKey(USER_ID, ALBUM_KEY)).thenReturn(null);
        when(userPhotoManager.countEnabledByUserIdAndType(USER_ID, PhotoType.ALBUM.name())).thenReturn(0L);
        when(businessIdGenerator.nextId()).thenReturn(9002L);
        when(userPhotoManager.findByUserIdAndObjectKey(USER_ID, ALBUM_KEY)).thenReturn(null, buildPhoto(9002L, ALBUM_KEY, PhotoType.ALBUM.name(), 1));

        BindPhotoResult result = userPhotoService.bindUserPhoto(buildBindCommand(PhotoType.ALBUM.name(), ALBUM_KEY, 1));

        assertEquals(ALBUM_KEY, result.getPhoto().getObjectKey());
        verify(userProfileManager, never()).updateAvatarKey(anyLong(), anyString());
    }

    @Test
    void bindShouldRejectOtherUserObjectKey() {
        mockActiveUserAndProfile(1);
        String otherKey = "avatar/999/202606/a.jpg";
        UserBizException ex = assertThrows(UserBizException.class,
                () -> userPhotoService.bindUserPhoto(buildBindCommand(PhotoType.AVATAR.name(), otherKey, 0)));
        assertEquals(UserErrorCode.PHOTO_OBJECT_KEY_INVALID, ex.getErrorCode());
    }

    @Test
    void bindShouldRejectFullUrlObjectKey() {
        mockActiveUserAndProfile(1);
        UserBizException ex = assertThrows(UserBizException.class,
                () -> userPhotoService.bindUserPhoto(buildBindCommand(PhotoType.AVATAR.name(),
                        "https://cdn.example.com/" + AVATAR_KEY, 0)));
        assertEquals(UserErrorCode.PHOTO_OBJECT_KEY_INVALID, ex.getErrorCode());
    }

    @Test
    void bindShouldRejectPathTraversalObjectKey() {
        mockActiveUserAndProfile(1);
        UserBizException ex = assertThrows(UserBizException.class,
                () -> userPhotoService.bindUserPhoto(buildBindCommand(PhotoType.AVATAR.name(),
                        "../" + AVATAR_KEY, 0)));
        assertEquals(UserErrorCode.PHOTO_OBJECT_KEY_INVALID, ex.getErrorCode());
    }

    @Test
    void bindShouldReuseExistingObjectKey() {
        mockActiveUserAndProfile(1);
        UserPhotoEntity existing = buildPhoto(8001L, AVATAR_KEY, PhotoType.AVATAR.name(), 0);
        when(userPhotoManager.findByUserIdAndObjectKey(USER_ID, AVATAR_KEY)).thenReturn(existing);

        userPhotoService.bindUserPhoto(buildBindCommand(PhotoType.AVATAR.name(), AVATAR_KEY, 0));

        verify(userPhotoManager, never()).createPhoto(anyLong(), anyLong(), anyString(), anyString(), anyInt(), anyString(), anyInt());
        verify(userPhotoManager).updatePhotoEnabledOrSort(existing, 1, 0);
    }

    @Test
    void bindAvatarShouldUpdateAvatarKey() {
        mockActiveUserAndProfile(1);
        when(userPhotoManager.findByUserIdAndObjectKey(USER_ID, AVATAR_KEY)).thenReturn(null);
        when(businessIdGenerator.nextId()).thenReturn(9003L);
        when(userPhotoManager.findByUserIdAndObjectKey(USER_ID, AVATAR_KEY)).thenReturn(null, buildPhoto(9003L, AVATAR_KEY, PhotoType.AVATAR.name(), 1));

        userPhotoService.bindUserPhoto(buildBindCommand(PhotoType.AVATAR.name(), AVATAR_KEY, 0));

        verify(userProfileManager).updateAvatarKey(USER_ID, AVATAR_KEY);
    }

    @Test
    void bindAvatarShouldDisablePreviousAvatar() {
        mockActiveUserAndProfile(1);
        when(userPhotoManager.findByUserIdAndObjectKey(USER_ID, AVATAR_KEY)).thenReturn(null);
        when(businessIdGenerator.nextId()).thenReturn(9004L);
        when(userPhotoManager.findByUserIdAndObjectKey(USER_ID, AVATAR_KEY)).thenReturn(null, buildPhoto(9004L, AVATAR_KEY, PhotoType.AVATAR.name(), 1));

        userPhotoService.bindUserPhoto(buildBindCommand(PhotoType.AVATAR.name(), AVATAR_KEY, 0));

        verify(userPhotoManager).disableCurrentAvatar(USER_ID);
    }

    @Test
    void bindAlbumShouldNotUpdateAvatarKey() {
        mockActiveUserAndProfile(1);
        when(userPhotoManager.findByUserIdAndObjectKey(USER_ID, ALBUM_KEY)).thenReturn(null);
        when(userPhotoManager.countEnabledByUserIdAndType(USER_ID, PhotoType.ALBUM.name())).thenReturn(0L);
        when(businessIdGenerator.nextId()).thenReturn(9005L);
        when(userPhotoManager.findByUserIdAndObjectKey(USER_ID, ALBUM_KEY)).thenReturn(null, buildPhoto(9005L, ALBUM_KEY, PhotoType.ALBUM.name(), 1));

        userPhotoService.bindUserPhoto(buildBindCommand(PhotoType.ALBUM.name(), ALBUM_KEY, 2));

        verify(userProfileManager, never()).updateAvatarKey(anyLong(), anyString());
    }

    @Test
    void listUserPhotosShouldReturnPhotos() {
        when(userManager.findByUserId(USER_ID)).thenReturn(buildUser(AccountStatus.ACTIVE.name()));
        when(userPhotoManager.listByUserIdAndType(USER_ID, null, false))
                .thenReturn(List.of(buildPhoto(1L, ALBUM_KEY, PhotoType.ALBUM.name(), 1)));

        ListUserPhotosQuery query = new ListUserPhotosQuery();
        query.setUserId(USER_ID);
        List<UserPhotoVO> photos = userPhotoService.listUserPhotos(query);

        assertEquals(1, photos.size());
        assertEquals(ALBUM_KEY, photos.get(0).getObjectKey());
    }

    @Test
    void listUserPhotosShouldSortBySortOrder() {
        when(userManager.findByUserId(USER_ID)).thenReturn(buildUser(AccountStatus.ACTIVE.name()));
        UserPhotoEntity first = buildPhoto(1L, ALBUM_KEY, PhotoType.ALBUM.name(), 1);
        first.setSortOrder(1);
        UserPhotoEntity second = buildPhoto(2L, "album/" + USER_ID + "/202606/c.jpg", PhotoType.ALBUM.name(), 1);
        second.setSortOrder(2);
        when(userPhotoManager.listByUserIdAndType(USER_ID, PhotoType.ALBUM.name(), false))
                .thenReturn(List.of(first, second));

        ListUserPhotosQuery query = new ListUserPhotosQuery();
        query.setUserId(USER_ID);
        query.setPhotoType(PhotoType.ALBUM.name());
        List<UserPhotoVO> photos = userPhotoService.listUserPhotos(query);

        assertEquals(1, photos.get(0).getSortOrder());
        assertEquals(2, photos.get(1).getSortOrder());
    }

    @Test
    void bindShouldRejectNonActiveUser() {
        when(userManager.findByUserId(USER_ID)).thenReturn(buildUser(AccountStatus.BANNED.name()));
        UserBizException ex = assertThrows(UserBizException.class,
                () -> userPhotoService.bindUserPhoto(buildBindCommand(PhotoType.AVATAR.name(), AVATAR_KEY, 0)));
        assertEquals(UserErrorCode.USER_BANNED, ex.getErrorCode());
    }

    @Test
    void bindAvatarWithCompletedProfileShouldSetPhotoDone() {
        mockActiveUserAndProfile(1);
        when(userPhotoManager.findByUserIdAndObjectKey(USER_ID, AVATAR_KEY)).thenReturn(null);
        when(businessIdGenerator.nextId()).thenReturn(9006L);
        when(userPhotoManager.findByUserIdAndObjectKey(USER_ID, AVATAR_KEY)).thenReturn(null, buildPhoto(9006L, AVATAR_KEY, PhotoType.AVATAR.name(), 1));

        BindPhotoResult result = userPhotoService.bindUserPhoto(buildBindCommand(PhotoType.AVATAR.name(), AVATAR_KEY, 0));

        assertEquals(ProfileStatus.PHOTO_DONE.name(), result.getProfileStatus());
        verify(userManager).updateProfileStatus(USER_ID, ProfileStatus.PHOTO_DONE.name());
    }

    @Test
    void bindAvatarWithIncompleteProfileShouldNotSetPhotoDone() {
        mockActiveUserAndProfile(0);
        when(userPhotoManager.findByUserIdAndObjectKey(USER_ID, AVATAR_KEY)).thenReturn(null);
        when(businessIdGenerator.nextId()).thenReturn(9007L);
        when(userPhotoManager.findByUserIdAndObjectKey(USER_ID, AVATAR_KEY)).thenReturn(null, buildPhoto(9007L, AVATAR_KEY, PhotoType.AVATAR.name(), 1));

        BindPhotoResult result = userPhotoService.bindUserPhoto(buildBindCommand(PhotoType.AVATAR.name(), AVATAR_KEY, 0));

        assertEquals(ProfileStatus.INIT.name(), result.getProfileStatus());
    }

    @Test
    void bindShouldNotSetCompletedStatus() {
        mockActiveUserAndProfile(1);
        when(userPhotoManager.findByUserIdAndObjectKey(USER_ID, AVATAR_KEY)).thenReturn(null);
        when(businessIdGenerator.nextId()).thenReturn(9008L);
        when(userPhotoManager.findByUserIdAndObjectKey(USER_ID, AVATAR_KEY)).thenReturn(null, buildPhoto(9008L, AVATAR_KEY, PhotoType.AVATAR.name(), 1));

        BindPhotoResult result = userPhotoService.bindUserPhoto(buildBindCommand(PhotoType.AVATAR.name(), AVATAR_KEY, 0));

        assertTrue(!ProfileStatus.COMPLETED.name().equals(result.getProfileStatus()));
    }

    @Test
    void bindShouldSucceedWhenRedisEvictFails() {
        StringRedisTemplate stringRedisTemplate = org.mockito.Mockito.mock(StringRedisTemplate.class);
        org.mockito.Mockito.when(stringRedisTemplate.delete(org.mockito.ArgumentMatchers.anyCollection()))
                .thenThrow(new RuntimeException("redis down"));
        UserPhotoService service = new UserPhotoServiceImpl(
                userManager,
                userProfileManager,
                userPhotoManager,
                photoObjectKeyValidator,
                profileStatusResolver,
                businessIdGenerator,
                new UserCacheInvalidationService(stringRedisTemplate)
        );
        mockActiveUserAndProfile(1);
        when(userPhotoManager.findByUserIdAndObjectKey(USER_ID, AVATAR_KEY)).thenReturn(null);
        when(businessIdGenerator.nextId()).thenReturn(9009L);
        when(userPhotoManager.findByUserIdAndObjectKey(USER_ID, AVATAR_KEY)).thenReturn(null, buildPhoto(9009L, AVATAR_KEY, PhotoType.AVATAR.name(), 1));

        BindPhotoResult result = service.bindUserPhoto(buildBindCommand(PhotoType.AVATAR.name(), AVATAR_KEY, 0));

        assertNotNull(result);
    }

    @Test
    void userPhotoVoShouldNotContainFullUrlFields() {
        Set<String> fields = Arrays.stream(UserPhotoVO.class.getDeclaredFields())
                .map(Field::getName)
                .collect(Collectors.toSet());
        assertTrue(fields.stream().noneMatch(name ->
                name.toLowerCase().contains("url")
                        || name.toLowerCase().contains("bucket")
                        || name.toLowerCase().contains("endpoint")
                        || name.toLowerCase().contains("presigned")));
    }

    @Test
    void userPhotoServiceShouldNotDependOnMinioClient() {
        Set<String> fields = Arrays.stream(UserPhotoServiceImpl.class.getDeclaredFields())
                .map(Field::getName)
                .collect(Collectors.toSet());
        assertTrue(fields.stream().noneMatch(name -> name.toLowerCase().contains("minio")));
    }

    @Test
    void profileStatusResolverShouldKeepPhotoDoneWhenAvatarExists() {
        assertEquals(ProfileStatus.PHOTO_DONE.name(), profileStatusResolver.resolve(1, AVATAR_KEY));
        assertEquals(ProfileStatus.BASIC_DONE.name(), profileStatusResolver.resolve(1, null));
        assertEquals(ProfileStatus.INIT.name(), profileStatusResolver.resolve(0, AVATAR_KEY));
    }

    private void mockActiveUserAndProfile(int profileCompleted) {
        when(userManager.findByUserId(USER_ID)).thenReturn(buildUser(AccountStatus.ACTIVE.name()));
        UserProfileEntity profile = buildProfile(profileCompleted);
        when(userProfileManager.findByUserId(USER_ID)).thenReturn(profile);
    }

    private BindPhotoCommand buildBindCommand(String photoType, String objectKey, int sortOrder) {
        BindPhotoCommand command = new BindPhotoCommand();
        command.setUserId(USER_ID);
        command.setPhotoType(photoType);
        command.setObjectKey(objectKey);
        command.setSortOrder(sortOrder);
        return command;
    }

    private UserEntity buildUser(String accountStatus) {
        UserEntity entity = new UserEntity();
        entity.setUserId(USER_ID);
        entity.setAccountStatus(accountStatus);
        entity.setProfileStatus(ProfileStatus.INIT.name());
        return entity;
    }

    private UserProfileEntity buildProfile(int profileCompleted) {
        UserProfileEntity entity = new UserProfileEntity();
        entity.setId(1L);
        entity.setUserId(USER_ID);
        entity.setProfileCompleted(profileCompleted);
        entity.setProfileScore(profileCompleted == 1 ? 100 : 0);
        entity.setAvatarKey(null);
        return entity;
    }

    private UserPhotoEntity buildPhoto(long photoId, String objectKey, String photoType, int enabled) {
        UserPhotoEntity entity = new UserPhotoEntity();
        entity.setId(photoId);
        entity.setPhotoId(photoId);
        entity.setUserId(USER_ID);
        entity.setObjectKey(objectKey);
        entity.setPhotoType(photoType);
        entity.setSortOrder(0);
        entity.setReviewStatus("PENDING");
        entity.setEnabled(enabled);
        entity.setCreatedAt(OffsetDateTime.now(ZoneOffset.UTC));
        entity.setUpdatedAt(OffsetDateTime.now(ZoneOffset.UTC));
        return entity;
    }
}
