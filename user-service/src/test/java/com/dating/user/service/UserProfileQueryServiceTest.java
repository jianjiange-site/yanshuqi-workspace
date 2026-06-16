package com.dating.user.service;

import com.dating.user.constant.AccountStatus;
import com.dating.user.constant.ProfileStatus;
import com.dating.user.dto.BatchGetBasicProfilesQuery;
import com.dating.user.dto.BatchGetRecommendProfilesQuery;
import com.dating.user.dto.CheckUserAvailableQuery;
import com.dating.user.entity.UserEntity;
import com.dating.user.entity.UserProfileEntity;
import com.dating.user.exception.UserBizException;
import com.dating.user.exception.UserErrorCode;
import com.dating.user.manager.UserManager;
import com.dating.user.manager.UserPhotoManager;
import com.dating.user.manager.UserProfileManager;
import com.dating.user.service.impl.UserProfileQueryServiceImpl;
import com.dating.user.service.support.BatchUserIdsValidator;
import com.dating.user.service.support.ProfileJsonSupport;
import com.dating.user.service.support.UserAvailabilityEvaluator;
import com.dating.user.vo.BasicUserProfileVO;
import com.dating.user.vo.RecommendUserProfileVO;
import com.dating.user.vo.UserAvailableVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 用户资料批量查询业务单元测试。
 */
@ExtendWith(MockitoExtension.class)
class UserProfileQueryServiceTest {

    private static final long USER_ID_1 = 1001L;

    private static final long USER_ID_2 = 1002L;

    private static final long MISSING_ID = 9999L;

    private static final String APPROVED_AVATAR = "avatar/1001/202606/a.jpg";

    @Mock
    private UserManager userManager;

    @Mock
    private UserProfileManager userProfileManager;

    @Mock
    private UserPhotoManager userPhotoManager;

    @Mock
    private UserProfileCacheService userProfileCacheService;

    private UserProfileQueryService userProfileQueryService;

    /**
     * 初始化测试依赖。
     */
    @BeforeEach
    void setUp() {
        userProfileQueryService = new UserProfileQueryServiceImpl(
                userManager,
                userProfileManager,
                userPhotoManager,
                userProfileCacheService,
                new BatchUserIdsValidator(),
                new UserAvailabilityEvaluator(),
                new ProfileJsonSupport()
        );
    }

    @Test
    void batchGetBasicProfilesShouldSucceed() {
        mockDbForActiveUser();
        when(userProfileCacheService.getBasicProfiles(anyList())).thenReturn(Map.of());
        when(userPhotoManager.listEnabledApprovedAvatarsByUserIds(anyList()))
                .thenReturn(Map.of(USER_ID_1, APPROVED_AVATAR));

        BatchGetBasicProfilesQuery query = new BatchGetBasicProfilesQuery();
        query.setUserIds(List.of(USER_ID_1));
        query.setIncludeUnavailable(true);
        List<BasicUserProfileVO> result = userProfileQueryService.batchGetBasicProfiles(query);

        assertEquals(1, result.size());
        assertEquals("U06昵称", result.get(0).getNickname());
        assertEquals(APPROVED_AVATAR, result.get(0).getAvatarKey());
        assertTrue(result.get(0).isAvailable());
    }

    @Test
    void batchGetRecommendProfilesShouldSucceed() {
        mockDbForActiveUser();
        when(userProfileCacheService.getRecommendProfiles(anyList())).thenReturn(Map.of());
        when(userPhotoManager.listEnabledApprovedAvatarsByUserIds(anyList()))
                .thenReturn(Map.of(USER_ID_1, APPROVED_AVATAR));

        BatchGetRecommendProfilesQuery query = new BatchGetRecommendProfilesQuery();
        query.setUserIds(List.of(USER_ID_1));
        query.setIncludeUnavailable(true);
        List<RecommendUserProfileVO> result = userProfileQueryService.batchGetRecommendProfiles(query);

        assertEquals(1, result.size());
        assertEquals("BH", result.get(0).getUserType());
        assertEquals(APPROVED_AVATAR, result.get(0).getAvatarKey());
    }

    @Test
    void checkUserAvailableShouldSucceed() {
        when(userProfileCacheService.getUserStatuses(anyList())).thenReturn(Map.of());
        when(userManager.listByUserIds(anyList())).thenReturn(List.of(buildActiveUser(USER_ID_1)));

        CheckUserAvailableQuery query = new CheckUserAvailableQuery();
        query.setUserIds(List.of(USER_ID_1));
        List<UserAvailableVO> result = userProfileQueryService.checkUserAvailable(query);

        assertEquals(1, result.size());
        assertTrue(result.get(0).isAvailable());
    }

    @Test
    void shouldDedupeUserIds() {
        mockDbForActiveUser();
        when(userProfileCacheService.getBasicProfiles(anyList())).thenReturn(Map.of());
        when(userPhotoManager.listEnabledApprovedAvatarsByUserIds(anyList())).thenReturn(Map.of());

        BatchGetBasicProfilesQuery query = new BatchGetBasicProfilesQuery();
        query.setUserIds(List.of(USER_ID_1, USER_ID_1, USER_ID_1));
        query.setIncludeUnavailable(true);
        List<BasicUserProfileVO> result = userProfileQueryService.batchGetBasicProfiles(query);

        assertEquals(1, result.size());
    }

    @Test
    void shouldFailWhenUserIdsEmpty() {
        BatchGetBasicProfilesQuery query = new BatchGetBasicProfilesQuery();
        query.setUserIds(List.of());
        UserBizException ex = assertThrows(UserBizException.class,
                () -> userProfileQueryService.batchGetBasicProfiles(query));
        assertEquals(UserErrorCode.USER_REQUEST_INVALID, ex.getErrorCode());
    }

    @Test
    void shouldFailWhenUserIdsExceedLimit() {
        List<Long> ids = java.util.stream.LongStream.rangeClosed(1, 101).boxed().toList();
        BatchGetBasicProfilesQuery query = new BatchGetBasicProfilesQuery();
        query.setUserIds(ids);
        UserBizException ex = assertThrows(UserBizException.class,
                () -> userProfileQueryService.batchGetBasicProfiles(query));
        assertEquals(UserErrorCode.USER_BATCH_SIZE_EXCEEDED, ex.getErrorCode());
    }

    @Test
    void activeUserShouldBeAvailable() {
        when(userProfileCacheService.getUserStatuses(anyList())).thenReturn(Map.of());
        when(userManager.listByUserIds(anyList())).thenReturn(List.of(buildActiveUser(USER_ID_1)));

        CheckUserAvailableQuery query = new CheckUserAvailableQuery();
        query.setUserIds(List.of(USER_ID_1));
        assertTrue(userProfileQueryService.checkUserAvailable(query).get(0).isAvailable());
    }

    @Test
    void bannedUserShouldBeUnavailable() {
        when(userProfileCacheService.getUserStatuses(anyList())).thenReturn(Map.of());
        UserEntity user = buildActiveUser(USER_ID_1);
        user.setAccountStatus(AccountStatus.BANNED.name());
        when(userManager.listByUserIds(anyList())).thenReturn(List.of(user));

        CheckUserAvailableQuery query = new CheckUserAvailableQuery();
        query.setUserIds(List.of(USER_ID_1));
        UserAvailableVO vo = userProfileQueryService.checkUserAvailable(query).get(0);
        assertFalse(vo.isAvailable());
        assertEquals(UserErrorCode.USER_BANNED.getCode(), vo.getReason());
    }

    @Test
    void deletedAccountStatusShouldBeUnavailable() {
        when(userProfileCacheService.getUserStatuses(anyList())).thenReturn(Map.of());
        UserEntity user = buildActiveUser(USER_ID_1);
        user.setAccountStatus(AccountStatus.DELETED.name());
        when(userManager.listByUserIds(anyList())).thenReturn(List.of(user));

        CheckUserAvailableQuery query = new CheckUserAvailableQuery();
        query.setUserIds(List.of(USER_ID_1));
        UserAvailableVO vo = userProfileQueryService.checkUserAvailable(query).get(0);
        assertEquals(UserErrorCode.USER_DELETED.getCode(), vo.getReason());
    }

    @Test
    void missingUserShouldBeUnavailable() {
        when(userProfileCacheService.getUserStatuses(anyList())).thenReturn(Map.of());
        when(userManager.listByUserIds(anyList())).thenReturn(List.of());

        CheckUserAvailableQuery query = new CheckUserAvailableQuery();
        query.setUserIds(List.of(MISSING_ID));
        UserAvailableVO vo = userProfileQueryService.checkUserAvailable(query).get(0);
        assertFalse(vo.isAvailable());
        assertEquals(UserErrorCode.USER_NOT_FOUND.getCode(), vo.getReason());
    }

    @Test
    void shouldFilterUnavailableWhenIncludeUnavailableFalse() {
        when(userProfileCacheService.getBasicProfiles(anyList())).thenReturn(Map.of());
        when(userManager.listByUserIds(anyList())).thenReturn(List.of(buildActiveUser(USER_ID_1)));
        UserEntity banned = buildActiveUser(USER_ID_2);
        banned.setAccountStatus(AccountStatus.BANNED.name());
        when(userProfileManager.listByUserIds(anyList()))
                .thenReturn(List.of(buildProfile(USER_ID_1), buildProfile(USER_ID_2)));
        when(userPhotoManager.listEnabledApprovedAvatarsByUserIds(anyList())).thenReturn(Map.of());

        BatchGetBasicProfilesQuery query = new BatchGetBasicProfilesQuery();
        query.setUserIds(List.of(USER_ID_1, USER_ID_2));
        query.setIncludeUnavailable(false);
        List<BasicUserProfileVO> result = userProfileQueryService.batchGetBasicProfiles(query);

        assertEquals(1, result.size());
        assertEquals(USER_ID_1, result.get(0).getUserId());
    }

    @Test
    void shouldIncludeUnavailableWhenRequested() {
        when(userProfileCacheService.getBasicProfiles(anyList())).thenReturn(Map.of());
        UserEntity banned = buildActiveUser(USER_ID_1);
        banned.setAccountStatus(AccountStatus.BANNED.name());
        when(userManager.listByUserIds(anyList())).thenReturn(List.of(banned));
        when(userProfileManager.listByUserIds(anyList())).thenReturn(List.of(buildProfile(USER_ID_1)));
        when(userPhotoManager.listEnabledApprovedAvatarsByUserIds(anyList())).thenReturn(Map.of());

        BatchGetBasicProfilesQuery query = new BatchGetBasicProfilesQuery();
        query.setUserIds(List.of(USER_ID_1));
        query.setIncludeUnavailable(true);
        BasicUserProfileVO vo = userProfileQueryService.batchGetBasicProfiles(query).get(0);

        assertFalse(vo.isAvailable());
        assertEquals(UserErrorCode.USER_BANNED.getCode(), vo.getUnavailableReason());
    }

    @Test
    void pendingAvatarShouldNotBeReturned() {
        mockDbForActiveUser();
        when(userProfileCacheService.getBasicProfiles(anyList())).thenReturn(Map.of());
        when(userPhotoManager.listEnabledApprovedAvatarsByUserIds(anyList())).thenReturn(Map.of());

        BatchGetBasicProfilesQuery query = new BatchGetBasicProfilesQuery();
        query.setUserIds(List.of(USER_ID_1));
        query.setIncludeUnavailable(true);
        BasicUserProfileVO vo = userProfileQueryService.batchGetBasicProfiles(query).get(0);

        assertNull(vo.getAvatarKey());
    }

    @Test
    void approvedAvatarShouldBeReturned() {
        mockDbForActiveUser();
        when(userProfileCacheService.getBasicProfiles(anyList())).thenReturn(Map.of());
        when(userPhotoManager.listEnabledApprovedAvatarsByUserIds(anyList()))
                .thenReturn(Map.of(USER_ID_1, APPROVED_AVATAR));

        BatchGetBasicProfilesQuery query = new BatchGetBasicProfilesQuery();
        query.setUserIds(List.of(USER_ID_1));
        query.setIncludeUnavailable(true);
        assertEquals(APPROVED_AVATAR, userProfileQueryService.batchGetBasicProfiles(query).get(0).getAvatarKey());
    }

    @Test
    void shouldNotContainFullUrlFields() {
        Set<String> fields = Arrays.stream(BasicUserProfileVO.class.getDeclaredFields())
                .map(Field::getName)
                .collect(Collectors.toSet());
        assertTrue(fields.stream().noneMatch(name ->
                name.toLowerCase().contains("url")
                        || name.toLowerCase().contains("bucket")
                        || name.toLowerCase().contains("endpoint")
                        || name.toLowerCase().contains("presigned")));
    }

    @Test
    void shouldUseCacheWithoutDbQuery() {
        BasicUserProfileVO cached = new BasicUserProfileVO();
        cached.setUserId(USER_ID_1);
        cached.setNickname("cached");
        cached.setAvailable(true);
        when(userProfileCacheService.getBasicProfiles(anyList())).thenReturn(Map.of(USER_ID_1, cached));

        BatchGetBasicProfilesQuery query = new BatchGetBasicProfilesQuery();
        query.setUserIds(List.of(USER_ID_1));
        query.setIncludeUnavailable(true);
        BasicUserProfileVO result = userProfileQueryService.batchGetBasicProfiles(query).get(0);

        assertEquals("cached", result.getNickname());
        verify(userManager, never()).listByUserIds(anyList());
    }

    @Test
    void shouldLoadDbAndWriteCacheOnMiss() {
        mockDbForActiveUser();
        when(userProfileCacheService.getBasicProfiles(anyList())).thenReturn(Map.of());
        when(userPhotoManager.listEnabledApprovedAvatarsByUserIds(anyList())).thenReturn(Map.of());

        BatchGetBasicProfilesQuery query = new BatchGetBasicProfilesQuery();
        query.setUserIds(List.of(USER_ID_1));
        query.setIncludeUnavailable(true);
        userProfileQueryService.batchGetBasicProfiles(query);

        verify(userManager).listByUserIds(anyList());
        verify(userProfileCacheService).putBasicProfiles(any());
    }

    @Test
    void blockedProfileStatusShouldBeUnavailable() {
        when(userProfileCacheService.getUserStatuses(anyList())).thenReturn(Map.of());
        UserEntity user = buildActiveUser(USER_ID_1);
        user.setProfileStatus(ProfileStatus.BLOCKED.name());
        when(userManager.listByUserIds(anyList())).thenReturn(List.of(user));

        CheckUserAvailableQuery query = new CheckUserAvailableQuery();
        query.setUserIds(List.of(USER_ID_1));
        UserAvailableVO vo = userProfileQueryService.checkUserAvailable(query).get(0);
        assertEquals("PROFILE_BLOCKED", vo.getReason());
    }

    @Test
    void queryServiceShouldNotDependOnMinio() {
        Set<String> fields = Arrays.stream(UserProfileQueryServiceImpl.class.getDeclaredFields())
                .map(Field::getName)
                .collect(Collectors.toSet());
        assertTrue(fields.stream().noneMatch(name -> name.toLowerCase().contains("minio")));
    }

    private void mockDbForActiveUser() {
        when(userManager.listByUserIds(anyList())).thenReturn(List.of(buildActiveUser(USER_ID_1)));
        when(userProfileManager.listByUserIds(anyList())).thenReturn(List.of(buildProfile(USER_ID_1)));
    }

    private UserEntity buildActiveUser(long userId) {
        UserEntity entity = new UserEntity();
        entity.setUserId(userId);
        entity.setUserType("BH");
        entity.setAccountStatus(AccountStatus.ACTIVE.name());
        entity.setProfileStatus(ProfileStatus.PHOTO_DONE.name());
        entity.setDeleted(0);
        return entity;
    }

    private UserProfileEntity buildProfile(long userId) {
        UserProfileEntity entity = new UserProfileEntity();
        entity.setUserId(userId);
        entity.setNickname("U06昵称");
        entity.setGender("MALE");
        entity.setCityCode("310000");
        entity.setBirthDate(LocalDate.of(1992, 1, 1));
        entity.setCountryCode("CN");
        entity.setBio("bio");
        entity.setProfileScore(100);
        entity.setProfileCompleted(1);
        return entity;
    }
}
