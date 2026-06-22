package com.dating.match.repository;

import com.dating.match.constant.RedisKeyConstants;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * 基于 Redis SET 的已划用户缓存。
 */
@Repository
@Profile("!test")
public class RedisSwipedSetRepository implements SwipedSetRepository {

    private final StringRedisTemplate stringRedisTemplate;

    public RedisSwipedSetRepository(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @Override
    public void addSwiped(long userId, long targetUserId) {
        stringRedisTemplate.opsForSet().add(RedisKeyConstants.swipedKey(userId), String.valueOf(targetUserId));
    }

    @Override
    public Set<Long> findSwipedTargets(long userId, Collection<Long> targetUserIds) {
        if (targetUserIds == null || targetUserIds.isEmpty()) {
            return Set.of();
        }
        String key = RedisKeyConstants.swipedKey(userId);
        Set<Long> swiped = new HashSet<>();
        for (Long targetUserId : targetUserIds) {
            if (targetUserId == null) {
                continue;
            }
            Boolean member = stringRedisTemplate.opsForSet().isMember(key, String.valueOf(targetUserId));
            if (Boolean.TRUE.equals(member)) {
                swiped.add(targetUserId);
            }
        }
        return swiped;
    }
}
