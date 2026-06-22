package com.dating.match.repository;

import com.dating.match.constant.RedisKeyConstants;
import com.dating.match.recommend.FeedQueueItem;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * 基于 Redis LIST 的 Feed 队列：D0 RPUSH，GetTodayFeed LPOP。
 */
@Repository
@Profile("!test")
public class RedisFeedQueueRepository implements FeedQueueRepository {

    private final StringRedisTemplate stringRedisTemplate;

    public RedisFeedQueueRepository(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @Override
    public void pushAll(long userId, List<FeedQueueItem> items, Duration ttl) {
        if (items == null || items.isEmpty()) {
            return;
        }
        String key = RedisKeyConstants.feedKey(userId);
        List<String> encoded = items.stream().map(FeedQueueItem::encode).toList();
        stringRedisTemplate.opsForList().rightPushAll(key, encoded);
        if (ttl != null && !ttl.isZero() && !ttl.isNegative()) {
            stringRedisTemplate.expire(key, ttl);
        }
    }

    @Override
    public void replaceAll(long userId, List<FeedQueueItem> items, Duration ttl) {
        String key = RedisKeyConstants.feedKey(userId);
        stringRedisTemplate.delete(key);
        if (items == null || items.isEmpty()) {
            return;
        }
        List<String> encoded = items.stream().map(FeedQueueItem::encode).toList();
        stringRedisTemplate.opsForList().rightPushAll(key, encoded);
        if (ttl != null && !ttl.isZero() && !ttl.isNegative()) {
            stringRedisTemplate.expire(key, ttl);
        }
    }

    @Override
    public List<FeedQueueItem> leftPop(long userId, int count) {
        if (count <= 0) {
            return List.of();
        }
        String key = RedisKeyConstants.feedKey(userId);
        List<FeedQueueItem> result = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            String value = stringRedisTemplate.opsForList().leftPop(key);
            if (value == null) {
                break;
            }
            result.add(FeedQueueItem.decode(value));
        }
        return result;
    }

    @Override
    public long size(long userId) {
        Long size = stringRedisTemplate.opsForList().size(RedisKeyConstants.feedKey(userId));
        return size == null ? 0L : size;
    }
}
