package com.dating.user.service.impl;

import com.dating.user.constant.RedisKeyConstants;
import com.dating.user.service.UserProfileCacheService;
import com.dating.user.vo.BasicUserProfileVO;
import com.dating.user.vo.RecommendUserProfileVO;
import com.dating.user.vo.UserAvailableVO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 用户资料 Redis 缓存服务实现。
 */
@Service
@Profile("!test")
public class UserProfileCacheServiceImpl implements UserProfileCacheService {

    private static final Logger log = LoggerFactory.getLogger(UserProfileCacheServiceImpl.class);

    private static final Duration CACHE_TTL = Duration.ofMinutes(10);

    private final StringRedisTemplate stringRedisTemplate;

    private final ObjectMapper objectMapper;

    /**
     * 构造用户资料缓存服务。
     *
     * @param stringRedisTemplate Redis 字符串模板
     */
    public UserProfileCacheServiceImpl(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    }

    /**
     * 批量读取基础资料缓存。
     *
     * @param userIds 用户业务主键列表
     * @return 命中的 userId 到 VO 映射
     */
    @Override
    public Map<Long, BasicUserProfileVO> getBasicProfiles(List<Long> userIds) {
        return multiGet(userIds, RedisKeyConstants::basicKey, BasicUserProfileVO.class);
    }

    /**
     * 批量写入基础资料缓存。
     *
     * @param profiles 基础资料 VO 映射
     */
    @Override
    public void putBasicProfiles(Map<Long, BasicUserProfileVO> profiles) {
        multiPut(profiles, RedisKeyConstants::basicKey);
    }

    /**
     * 批量读取推荐展示资料缓存。
     *
     * @param userIds 用户业务主键列表
     * @return 命中的 userId 到 VO 映射
     */
    @Override
    public Map<Long, RecommendUserProfileVO> getRecommendProfiles(List<Long> userIds) {
        return multiGet(userIds, RedisKeyConstants::profileKey, RecommendUserProfileVO.class);
    }

    /**
     * 批量写入推荐展示资料缓存。
     *
     * @param profiles 推荐展示资料 VO 映射
     */
    @Override
    public void putRecommendProfiles(Map<Long, RecommendUserProfileVO> profiles) {
        multiPut(profiles, RedisKeyConstants::profileKey);
    }

    /**
     * 批量读取用户状态缓存。
     *
     * @param userIds 用户业务主键列表
     * @return 命中的 userId 到 VO 映射
     */
    @Override
    public Map<Long, UserAvailableVO> getUserStatuses(List<Long> userIds) {
        return multiGet(userIds, RedisKeyConstants::statusKey, UserAvailableVO.class);
    }

    /**
     * 批量写入用户状态缓存。
     *
     * @param statuses 用户可用性 VO 映射
     */
    @Override
    public void putUserStatuses(Map<Long, UserAvailableVO> statuses) {
        multiPut(statuses, RedisKeyConstants::statusKey);
    }

    private <T> Map<Long, T> multiGet(List<Long> userIds, java.util.function.LongFunction<String> keyFn, Class<T> type) {
        Map<Long, T> result = new HashMap<>();
        if (userIds == null || userIds.isEmpty()) {
            return result;
        }
        try {
            List<String> keys = userIds.stream().map(id -> keyFn.apply(id)).toList();
            List<String> values = stringRedisTemplate.opsForValue().multiGet(keys);
            if (values == null) {
                return result;
            }
            for (int i = 0; i < userIds.size(); i++) {
                String json = values.get(i);
                if (json == null || json.isBlank()) {
                    continue;
                }
                try {
                    T value = objectMapper.readValue(json, type);
                    result.put(userIds.get(i), value);
                } catch (JsonProcessingException ex) {
                    log.warn("用户资料缓存反序列化失败, userId={}", userIds.get(i));
                }
            }
        } catch (Exception ex) {
            log.warn("用户资料缓存批量读取失败", ex);
        }
        return result;
    }

    private <T> void multiPut(Map<Long, T> values, java.util.function.LongFunction<String> keyFn) {
        if (values == null || values.isEmpty()) {
            return;
        }
        for (Map.Entry<Long, T> entry : values.entrySet()) {
            if (entry.getKey() == null || entry.getValue() == null) {
                continue;
            }
            try {
                String json = objectMapper.writeValueAsString(entry.getValue());
                stringRedisTemplate.opsForValue().set(keyFn.apply(entry.getKey()), json, CACHE_TTL);
            } catch (Exception ex) {
                log.warn("用户资料缓存写入失败, userId={}", entry.getKey(), ex);
            }
        }
    }
}
