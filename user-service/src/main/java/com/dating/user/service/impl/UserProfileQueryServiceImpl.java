package com.dating.user.service.impl;

import com.dating.user.dto.BatchGetBasicProfilesQuery;
import com.dating.user.dto.BatchGetRecommendProfilesQuery;
import com.dating.user.dto.CheckUserAvailableQuery;
import com.dating.user.entity.UserEntity;
import com.dating.user.entity.UserProfileEntity;
import com.dating.user.exception.UserBizException;
import com.dating.user.manager.UserManager;
import com.dating.user.manager.UserPhotoManager;
import com.dating.user.manager.UserProfileManager;
import com.dating.user.service.UserProfileCacheService;
import com.dating.user.service.UserProfileQueryService;
import com.dating.user.service.support.BatchUserIdsValidator;
import com.dating.user.service.support.ProfileJsonSupport;
import com.dating.user.service.support.SlowCallLogger;
import com.dating.user.service.support.UserAvailabilityEvaluator;
import com.dating.user.vo.BasicUserProfileVO;
import com.dating.user.vo.RecommendUserProfileVO;
import com.dating.user.vo.UserAvailableVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 用户资料批量只读查询服务实现。
 */
@Service
@Profile("!test")
public class UserProfileQueryServiceImpl implements UserProfileQueryService {

    private static final Logger log = LoggerFactory.getLogger(UserProfileQueryServiceImpl.class);

    private final UserManager userManager;
    private final UserProfileManager userProfileManager;
    private final UserPhotoManager userPhotoManager;
    private final UserProfileCacheService userProfileCacheService;
    private final BatchUserIdsValidator batchUserIdsValidator;
    private final UserAvailabilityEvaluator userAvailabilityEvaluator;
    private final ProfileJsonSupport profileJsonSupport;
    private final SlowCallLogger slowCallLogger;

    /**
     * 构造用户资料批量查询服务。
     *
     * @param userManager               用户主表 Manager
     * @param userProfileManager        用户资料 Manager
     * @param userPhotoManager          用户照片 Manager
     * @param userProfileCacheService   资料缓存服务
     * @param batchUserIdsValidator     批量 ID 校验器
     * @param userAvailabilityEvaluator 可用性判断器
     * @param profileJsonSupport        资料 JSON 支持
     * @param slowCallLogger            慢调用日志记录器
     */
    public UserProfileQueryServiceImpl(UserManager userManager,
                                       UserProfileManager userProfileManager,
                                       UserPhotoManager userPhotoManager,
                                       UserProfileCacheService userProfileCacheService,
                                       BatchUserIdsValidator batchUserIdsValidator,
                                       UserAvailabilityEvaluator userAvailabilityEvaluator,
                                       ProfileJsonSupport profileJsonSupport,
                                       SlowCallLogger slowCallLogger) {
        this.userManager = userManager;
        this.userProfileManager = userProfileManager;
        this.userPhotoManager = userPhotoManager;
        this.userProfileCacheService = userProfileCacheService;
        this.batchUserIdsValidator = batchUserIdsValidator;
        this.userAvailabilityEvaluator = userAvailabilityEvaluator;
        this.profileJsonSupport = profileJsonSupport;
        this.slowCallLogger = slowCallLogger;
    }

    /**
     * 批量查询用户基础资料。
     *
     * @param query 查询条件
     * @return 基础资料 VO 列表
     */
    @Override
    @Transactional(readOnly = true)
    public List<BasicUserProfileVO> batchGetBasicProfiles(BatchGetBasicProfilesQuery query) {
        long startNano = System.nanoTime();
        String method = "batchGetBasicProfiles";
        boolean success = true;
        String errorCode = null;
        int userIdsSize = 0;
        int cacheHit = 0;
        int cacheMiss = 0;
        try {
            // 1. 参数校验：userIds 非空、去重、数量 1-100
            List<Long> orderedUserIds = batchUserIdsValidator.validateAndDedupe(query.getUserIds());
            userIdsSize = orderedUserIds.size();

            // 2. 批量读取 Redis basic 缓存
            Map<Long, BasicUserProfileVO> cached = new HashMap<>(userProfileCacheService.getBasicProfiles(orderedUserIds));
            cacheHit = cached.size();

            // 3. 计算未命中 userIds
            List<Long> missedUserIds = orderedUserIds.stream()
                    .filter(userId -> !cached.containsKey(userId))
                    .toList();
            cacheMiss = missedUserIds.size();

            // 4. 未命中部分批量查 DB 并写缓存
            if (!missedUserIds.isEmpty()) {
                Map<Long, BasicUserProfileVO> loaded = loadBasicProfilesFromDb(missedUserIds);
                userProfileCacheService.putBasicProfiles(loaded);
                cached.putAll(loaded);
            }

            // 5. 按输入顺序合并结果，并按 includeUnavailable 过滤
            List<BasicUserProfileVO> result = buildOrderedResult(orderedUserIds, cached, query.isIncludeUnavailable());
            logBatchQuery(method, userIdsSize, cacheHit, cacheMiss, startNano);
            return result;
        } catch (UserBizException ex) {
            success = false;
            errorCode = ex.getErrorCode().getCode();
            throw ex;
        } finally {
            slowCallLogger.logBatchIfSlow(method, startNano, userIdsSize, cacheHit, cacheMiss, success, errorCode);
        }
    }

    /**
     * 批量查询用户推荐展示资料。
     *
     * @param query 查询条件
     * @return 推荐展示资料 VO 列表
     */
    @Override
    @Transactional(readOnly = true)
    public List<RecommendUserProfileVO> batchGetRecommendProfiles(BatchGetRecommendProfilesQuery query) {
        long startNano = System.nanoTime();
        String method = "batchGetRecommendProfiles";
        boolean success = true;
        String errorCode = null;
        int userIdsSize = 0;
        int cacheHit = 0;
        int cacheMiss = 0;
        try {
            // 1. 参数校验
            List<Long> orderedUserIds = batchUserIdsValidator.validateAndDedupe(query.getUserIds());
            userIdsSize = orderedUserIds.size();

            // 2. 批量读取 Redis profile 缓存
            Map<Long, RecommendUserProfileVO> cached = new HashMap<>(userProfileCacheService.getRecommendProfiles(orderedUserIds));
            cacheHit = cached.size();

            // 3. 未命中部分查 DB
            List<Long> missedUserIds = orderedUserIds.stream()
                    .filter(userId -> !cached.containsKey(userId))
                    .toList();
            cacheMiss = missedUserIds.size();
            if (!missedUserIds.isEmpty()) {
                Map<Long, RecommendUserProfileVO> loaded = loadRecommendProfilesFromDb(missedUserIds);
                userProfileCacheService.putRecommendProfiles(loaded);
                cached.putAll(loaded);
            }

            // 4. 按顺序返回，过滤不可用用户
            List<RecommendUserProfileVO> result = new ArrayList<>();
            for (Long userId : orderedUserIds) {
                RecommendUserProfileVO vo = cached.get(userId);
                if (vo == null) {
                    continue;
                }
                if (!query.isIncludeUnavailable() && !vo.isAvailable()) {
                    continue;
                }
                result.add(vo);
            }
            logBatchQuery(method, userIdsSize, cacheHit, cacheMiss, startNano);
            return result;
        } catch (UserBizException ex) {
            success = false;
            errorCode = ex.getErrorCode().getCode();
            throw ex;
        } finally {
            slowCallLogger.logBatchIfSlow(method, startNano, userIdsSize, cacheHit, cacheMiss, success, errorCode);
        }
    }

    /**
     * 批量检查用户是否可用。
     *
     * @param query 查询条件
     * @return 用户可用性 VO 列表
     */
    @Override
    @Transactional(readOnly = true)
    public List<UserAvailableVO> checkUserAvailable(CheckUserAvailableQuery query) {
        long startNano = System.nanoTime();
        String method = "checkUserAvailable";
        boolean success = true;
        String errorCode = null;
        int userIdsSize = 0;
        int cacheHit = 0;
        int cacheMiss = 0;
        try {
            // 1. 参数校验
            List<Long> orderedUserIds = batchUserIdsValidator.validateAndDedupe(query.getUserIds());
            userIdsSize = orderedUserIds.size();

            // 2. 批量读取 Redis status 缓存
            Map<Long, UserAvailableVO> cached = new HashMap<>(userProfileCacheService.getUserStatuses(orderedUserIds));
            cacheHit = cached.size();

            // 3. 未命中部分查 users
            List<Long> missedUserIds = orderedUserIds.stream()
                    .filter(userId -> !cached.containsKey(userId))
                    .toList();
            cacheMiss = missedUserIds.size();
            if (!missedUserIds.isEmpty()) {
                Map<Long, UserAvailableVO> loaded = loadUserStatusesFromDb(missedUserIds);
                userProfileCacheService.putUserStatuses(loaded);
                cached.putAll(loaded);
            }

            // 4. 按输入顺序返回每个 userId 的可用性
            List<UserAvailableVO> result = new ArrayList<>();
            for (Long userId : orderedUserIds) {
                UserAvailableVO vo = cached.get(userId);
                if (vo == null) {
                    vo = buildUnavailableStatus(userId, UserAvailabilityEvaluator.AvailabilityResult
                            .unavailable(com.dating.user.exception.UserErrorCode.USER_NOT_FOUND.getCode()));
                }
                result.add(vo);
            }
            logBatchQuery(method, userIdsSize, cacheHit, cacheMiss, startNano);
            return result;
        } catch (UserBizException ex) {
            success = false;
            errorCode = ex.getErrorCode().getCode();
            throw ex;
        } finally {
            slowCallLogger.logBatchIfSlow(method, startNano, userIdsSize, cacheHit, cacheMiss, success, errorCode);
        }
    }

    private void logBatchQuery(String method, int userIdsSize, int cacheHit, int cacheMiss, long startNano) {
        long costMs = (System.nanoTime() - startNano) / 1_000_000L;
        log.info("批量资料查询, method={}, userIdsSize={}, cacheHit={}, cacheMiss={}, costMs={}",
                method, userIdsSize, cacheHit, cacheMiss, costMs);
    }

    private Map<Long, BasicUserProfileVO> loadBasicProfilesFromDb(List<Long> userIds) {
        Map<Long, UserEntity> users = toMap(userManager.listByUserIds(userIds), UserEntity::getUserId);
        Map<Long, UserProfileEntity> profiles = toMap(userProfileManager.listByUserIds(userIds), UserProfileEntity::getUserId);
        Map<Long, String> approvedAvatars = userPhotoManager.listEnabledApprovedAvatarsByUserIds(userIds);

        Map<Long, BasicUserProfileVO> result = new HashMap<>();
        for (Long userId : userIds) {
            UserEntity user = users.get(userId);
            UserProfileEntity profile = profiles.get(userId);
            UserAvailabilityEvaluator.AvailabilityResult availability = userAvailabilityEvaluator.evaluate(user);
            BasicUserProfileVO vo = new BasicUserProfileVO();
            vo.setUserId(userId);
            vo.setAvailable(availability.isAvailable());
            vo.setUnavailableReason(availability.getReason());
            if (user != null) {
                vo.setAccountStatus(user.getAccountStatus());
                vo.setProfileStatus(user.getProfileStatus());
            }
            if (profile != null) {
                vo.setNickname(profile.getNickname());
                vo.setGender(profile.getGender());
                vo.setCityCode(profile.getCityCode());
            }
            vo.setAvatarKey(approvedAvatars.get(userId));
            result.put(userId, vo);
        }
        return result;
    }

    private Map<Long, RecommendUserProfileVO> loadRecommendProfilesFromDb(List<Long> userIds) {
        Map<Long, UserEntity> users = toMap(userManager.listByUserIds(userIds), UserEntity::getUserId);
        Map<Long, UserProfileEntity> profiles = toMap(userProfileManager.listByUserIds(userIds), UserProfileEntity::getUserId);
        Map<Long, String> approvedAvatars = userPhotoManager.listEnabledApprovedAvatarsByUserIds(userIds);

        Map<Long, RecommendUserProfileVO> result = new HashMap<>();
        for (Long userId : userIds) {
            UserEntity user = users.get(userId);
            UserProfileEntity profile = profiles.get(userId);
            UserAvailabilityEvaluator.AvailabilityResult availability = userAvailabilityEvaluator.evaluate(user);
            RecommendUserProfileVO vo = new RecommendUserProfileVO();
            vo.setUserId(userId);
            vo.setAvailable(availability.isAvailable());
            vo.setUnavailableReason(availability.getReason());
            if (user != null) {
                vo.setUserType(user.getUserType());
                vo.setAccountStatus(user.getAccountStatus());
                vo.setProfileStatus(user.getProfileStatus());
            }
            if (profile != null) {
                vo.setGender(profile.getGender());
                vo.setBirthDate(profile.getBirthDate());
                vo.setCountryCode(profile.getCountryCode());
                vo.setCityCode(profile.getCityCode());
                vo.setLanguageCodes(profileJsonSupport.fromJsonArray(profile.getLanguageCodes()));
                vo.setInterests(profileJsonSupport.fromJsonArray(profile.getInterests()));
                vo.setBio(profile.getBio());
                vo.setProfileScore(profile.getProfileScore());
                vo.setProfileCompleted(profile.getProfileCompleted());
            }
            vo.setAvatarKey(approvedAvatars.get(userId));
            result.put(userId, vo);
        }
        return result;
    }

    private Map<Long, UserAvailableVO> loadUserStatusesFromDb(List<Long> userIds) {
        Map<Long, UserEntity> users = toMap(userManager.listByUserIds(userIds), UserEntity::getUserId);
        Map<Long, UserAvailableVO> result = new HashMap<>();
        for (Long userId : userIds) {
            UserEntity user = users.get(userId);
            UserAvailabilityEvaluator.AvailabilityResult availability = userAvailabilityEvaluator.evaluate(user);
            result.put(userId, buildUserAvailableVO(userId, user, availability));
        }
        return result;
    }

    private UserAvailableVO buildUserAvailableVO(Long userId,
                                                 UserEntity user,
                                                 UserAvailabilityEvaluator.AvailabilityResult availability) {
        UserAvailableVO vo = new UserAvailableVO();
        vo.setUserId(userId);
        vo.setAvailable(availability.isAvailable());
        vo.setReason(availability.getReason());
        if (user != null) {
            vo.setAccountStatus(user.getAccountStatus());
            vo.setProfileStatus(user.getProfileStatus());
        }
        return vo;
    }

    private UserAvailableVO buildUnavailableStatus(Long userId,
                                                 UserAvailabilityEvaluator.AvailabilityResult availability) {
        UserAvailableVO vo = new UserAvailableVO();
        vo.setUserId(userId);
        vo.setAvailable(availability.isAvailable());
        vo.setReason(availability.getReason());
        return vo;
    }

    private List<BasicUserProfileVO> buildOrderedResult(List<Long> orderedUserIds,
                                                        Map<Long, BasicUserProfileVO> cached,
                                                        boolean includeUnavailable) {
        List<BasicUserProfileVO> result = new ArrayList<>();
        for (Long userId : orderedUserIds) {
            BasicUserProfileVO vo = cached.get(userId);
            if (vo == null) {
                continue;
            }
            if (!includeUnavailable && !vo.isAvailable()) {
                continue;
            }
            result.add(vo);
        }
        return result;
    }

    private <T> Map<Long, T> toMap(List<T> items, Function<T, Long> keyExtractor) {
        if (items == null || items.isEmpty()) {
            return Map.of();
        }
        return items.stream().collect(Collectors.toMap(keyExtractor, Function.identity(), (a, b) -> a));
    }
}
