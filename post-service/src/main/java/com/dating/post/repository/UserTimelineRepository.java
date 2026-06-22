package com.dating.post.repository;

import com.dating.post.constant.PostRedisKeys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * 好友时间线 Redis 操作：发帖后写扩散到好友 timeline ZSet。
 */
@Repository
public class UserTimelineRepository {

    private static final Logger log = LoggerFactory.getLogger(UserTimelineRepository.class);

    private final StringRedisTemplate stringRedisTemplate;

    public UserTimelineRepository(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    public void addToTimeline(long userId, long postId, double score) {
        try {
            String key = PostRedisKeys.userTimeline(userId);
            stringRedisTemplate.opsForZSet().add(key, String.valueOf(postId), score);
            Long size = stringRedisTemplate.opsForZSet().size(key);
            if (size != null && size > PostRedisKeys.TIMELINE_SIZE) {
                long removeCount = size - PostRedisKeys.TIMELINE_SIZE;
                stringRedisTemplate.opsForZSet().removeRange(key, 0, removeCount - 1);
            }
            stringRedisTemplate.expire(key, Duration.ofSeconds(PostRedisKeys.TTL_SECONDS));
        } catch (Exception ex) {
            log.warn("写入好友时间线失败, userId={}, postId={}, error={}", userId, postId, ex.getMessage());
        }
    }

    public List<Long> listTimeline(long userId, int limit) {
        try {
            if (limit <= 0) {
                return List.of();
            }
            Set<String> members = stringRedisTemplate.opsForZSet()
                    .reverseRange(PostRedisKeys.userTimeline(userId), 0, limit - 1L);
            if (members == null || members.isEmpty()) {
                return List.of();
            }
            List<Long> result = new ArrayList<>(members.size());
            for (String member : members) {
                result.add(Long.parseLong(member));
            }
            return result;
        } catch (Exception ex) {
            log.warn("读取好友时间线失败, userId={}, error={}", userId, ex.getMessage());
            return List.of();
        }
    }
}
