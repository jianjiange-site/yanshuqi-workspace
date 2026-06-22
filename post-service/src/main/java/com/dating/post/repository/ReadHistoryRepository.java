package com.dating.post.repository;

import com.dating.post.constant.PostRedisKeys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 已读帖子 Redis Set 操作。
 */
@Repository
public class ReadHistoryRepository {

    private static final Logger log = LoggerFactory.getLogger(ReadHistoryRepository.class);

    private final StringRedisTemplate stringRedisTemplate;

    public ReadHistoryRepository(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    public Set<Long> listReadPostIds(long userId) {
        try {
            Set<String> members = stringRedisTemplate.opsForSet().members(PostRedisKeys.readPosts(userId));
            if (members == null || members.isEmpty()) {
                return Set.of();
            }
            Set<Long> result = new HashSet<>(members.size());
            for (String member : members) {
                result.add(Long.parseLong(member));
            }
            return result;
        } catch (Exception ex) {
            log.warn("读取已读集合失败, userId={}, error={}", userId, ex.getMessage());
            return Set.of();
        }
    }

    public void markRead(long userId, Collection<Long> postIds) {
        if (postIds == null || postIds.isEmpty()) {
            return;
        }
        try {
            String key = PostRedisKeys.readPosts(userId);
            String[] values = postIds.stream().map(String::valueOf).toArray(String[]::new);
            stringRedisTemplate.opsForSet().add(key, values);
            stringRedisTemplate.expire(key, Duration.ofSeconds(PostRedisKeys.TTL_SECONDS));
        } catch (Exception ex) {
            log.warn("写入已读集合失败, userId={}, error={}", userId, ex.getMessage());
        }
    }
}
