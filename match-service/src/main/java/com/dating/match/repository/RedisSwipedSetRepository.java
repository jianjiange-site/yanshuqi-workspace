package com.dating.match.repository;

import com.dating.match.constant.RedisKeyConstants;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

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
}
