package com.dating.post.repository;

import com.dating.post.constant.PostRedisKeys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Redis 计数增量、评论窗口、待刷盘集合访问层。
 */
@Repository
public class PostStatDeltaRepository {

    private static final Logger log = LoggerFactory.getLogger(PostStatDeltaRepository.class);

    /**
     * Lua 原子读取 delta 并置 0。
     * <p>
     * 不能拆成客户端 GET + SET 两条命令：刷盘期间若有 INCR，SET 0 可能覆盖新增增量导致丢计数。
     * Lua 在 Redis 单线程中原子执行，取走当前值后再归零，期间新增的 INCR 会保留在新值中。
     */
    private static final DefaultRedisScript<Long> ATOMIC_GET_AND_RESET = new DefaultRedisScript<>(
            """
                    local current = redis.call('GET', KEYS[1])
                    if not current then
                      return 0
                    end
                    redis.call('SET', KEYS[1], '0')
                    redis.call('EXPIRE', KEYS[1], ARGV[1])
                    return tonumber(current)
                    """,
            Long.class);

    private final StringRedisTemplate stringRedisTemplate;

    public PostStatDeltaRepository(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    public void incrementLikeDelta(long postId, int delta) {
        incrementDelta(PostRedisKeys.statIncrLikes(postId), delta);
        markUpdated(postId);
    }

    public void incrementCommentDelta(long postId, int delta) {
        incrementDelta(PostRedisKeys.statIncrComments(postId), delta);
        markUpdated(postId);
    }

    public int readLikeDelta(long postId) {
        return readDelta(PostRedisKeys.statIncrLikes(postId));
    }

    public int readCommentDelta(long postId) {
        return readDelta(PostRedisKeys.statIncrComments(postId));
    }

    public long atomicTakeLikeDelta(long postId) {
        return atomicTakeDelta(PostRedisKeys.statIncrLikes(postId));
    }

    public long atomicTakeCommentDelta(long postId) {
        return atomicTakeDelta(PostRedisKeys.statIncrComments(postId));
    }

    public Set<Long> listUpdatedPostIds() {
        try {
            Set<String> members = stringRedisTemplate.opsForSet().members(PostRedisKeys.updatedSet());
            if (members == null || members.isEmpty()) {
                return Set.of();
            }
            return members.stream().map(Long::parseLong).collect(Collectors.toSet());
        } catch (Exception ex) {
            log.warn("读取 updated_set 失败, error={}", ex.getMessage());
            return Set.of();
        }
    }

    public void removeUpdatedPostIfIdle(long postId) {
        if (readLikeDelta(postId) == 0 && readCommentDelta(postId) == 0) {
            try {
                stringRedisTemplate.opsForSet().remove(PostRedisKeys.updatedSet(), String.valueOf(postId));
            } catch (Exception ex) {
                log.warn("移除 updated_set 失败, postId={}, error={}", postId, ex.getMessage());
            }
        }
    }

    public void addCommentToWindow(long postId, long commentId) {
        try {
            String key = PostRedisKeys.commentWindow(postId);
            stringRedisTemplate.opsForZSet().add(key, String.valueOf(commentId), commentId);
            Long size = stringRedisTemplate.opsForZSet().size(key);
            if (size != null && size > PostRedisKeys.COMMENT_WINDOW_SIZE) {
                long removeCount = size - PostRedisKeys.COMMENT_WINDOW_SIZE;
                stringRedisTemplate.opsForZSet().removeRange(key, 0, removeCount - 1);
            }
            stringRedisTemplate.expire(key, Duration.ofSeconds(PostRedisKeys.TTL_SECONDS));
        } catch (Exception ex) {
            log.warn("写入评论窗口失败, postId={}, commentId={}, error={}", postId, commentId, ex.getMessage());
        }
    }

    public Set<Long> listCommentIdsFromWindow(long postId, long cursorCommentId, int limit) {
        try {
            String key = PostRedisKeys.commentWindow(postId);
            double max = cursorCommentId > 0 ? cursorCommentId - 1 : Double.MAX_VALUE;
            Set<String> members = stringRedisTemplate.opsForZSet()
                    .reverseRangeByScore(key, 0, max, 0, limit);
            if (members == null || members.isEmpty()) {
                return Set.of();
            }
            return members.stream().map(Long::parseLong).collect(Collectors.toCollection(java.util.LinkedHashSet::new));
        } catch (Exception ex) {
            log.warn("读取评论窗口失败, postId={}, error={}", postId, ex.getMessage());
            return Set.of();
        }
    }

    public void removeCommentFromWindow(long postId, long commentId) {
        try {
            stringRedisTemplate.opsForZSet().remove(PostRedisKeys.commentWindow(postId), String.valueOf(commentId));
        } catch (Exception ex) {
            log.warn("移除评论窗口失败, postId={}, commentId={}, error={}", postId, commentId, ex.getMessage());
        }
    }

    private void incrementDelta(String key, int delta) {
        try {
            stringRedisTemplate.opsForValue().increment(key, delta);
            stringRedisTemplate.expire(key, Duration.ofSeconds(PostRedisKeys.TTL_SECONDS));
        } catch (Exception ex) {
            log.warn("写入 Redis delta 失败, key={}, delta={}, error={}", key, delta, ex.getMessage());
        }
    }

    private int readDelta(String key) {
        try {
            String value = stringRedisTemplate.opsForValue().get(key);
            if (value == null || value.isBlank()) {
                return 0;
            }
            return Integer.parseInt(value);
        } catch (Exception ex) {
            log.warn("读取 Redis delta 失败, key={}, error={}", key, ex.getMessage());
            return 0;
        }
    }

    private long atomicTakeDelta(String key) {
        try {
            Long value = stringRedisTemplate.execute(
                    ATOMIC_GET_AND_RESET,
                    Collections.singletonList(key),
                    String.valueOf(PostRedisKeys.TTL_SECONDS));
            return value == null ? 0L : value;
        } catch (Exception ex) {
            log.warn("Lua 原子取 delta 失败, key={}, error={}", key, ex.getMessage());
            return 0L;
        }
    }

    private void markUpdated(long postId) {
        try {
            stringRedisTemplate.opsForSet().add(PostRedisKeys.updatedSet(), String.valueOf(postId));
            stringRedisTemplate.expire(PostRedisKeys.updatedSet(), Duration.ofSeconds(PostRedisKeys.TTL_SECONDS));
        } catch (Exception ex) {
            log.warn("写入 updated_set 失败, postId={}, error={}", postId, ex.getMessage());
        }
    }
}
