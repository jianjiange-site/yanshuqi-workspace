package com.dating.post.repository;

import com.dating.post.constant.GenderBucket;
import com.dating.post.constant.PostRedisKeys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Feed 热门池 / 冷启动池 Redis 操作。
 */
@Repository
public class FeedPoolRepository {

    private static final Logger log = LoggerFactory.getLogger(FeedPoolRepository.class);

    private final StringRedisTemplate stringRedisTemplate;

    public FeedPoolRepository(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    public void addToColdStartPool(GenderBucket bucket, long postId, double score) {
        try {
            String key = PostRedisKeys.coldStartPool(bucket);
            stringRedisTemplate.opsForZSet().add(key, String.valueOf(postId), score);
            trimPool(key, PostRedisKeys.COLD_START_POOL_SIZE);
            stringRedisTemplate.expire(key, Duration.ofSeconds(PostRedisKeys.TTL_SECONDS));
        } catch (Exception ex) {
            log.warn("写入冷启动池失败, bucket={}, postId={}, error={}", bucket, postId, ex.getMessage());
        }
    }

    public List<Long> listRecommendPool(GenderBucket bucket, int offset, int limit) {
        return listZSetDesc(PostRedisKeys.recommendPool(bucket), offset, limit);
    }

    public List<Long> listColdStartPool(GenderBucket bucket, int offset, int limit) {
        return listZSetDesc(PostRedisKeys.coldStartPool(bucket), offset, limit);
    }

    /**
     * 写入临时热门池，完成后 rename 原子替换正式池，避免重建期间读池为空或半成品。
     */
    public void rebuildRecommendPool(GenderBucket bucket, Map<Long, Double> scoredPosts) {
        String tmpKey = PostRedisKeys.recommendPoolTmp(bucket);
        String officialKey = PostRedisKeys.recommendPool(bucket);
        try {
            stringRedisTemplate.delete(tmpKey);
            if (scoredPosts != null && !scoredPosts.isEmpty()) {
                for (Map.Entry<Long, Double> entry : scoredPosts.entrySet()) {
                    stringRedisTemplate.opsForZSet().add(tmpKey, String.valueOf(entry.getKey()), entry.getValue());
                }
                trimPool(tmpKey, PostRedisKeys.RECOMMEND_POOL_SIZE);
            }
            stringRedisTemplate.expire(tmpKey, Duration.ofSeconds(PostRedisKeys.TTL_SECONDS));
            if (Boolean.TRUE.equals(stringRedisTemplate.hasKey(tmpKey))) {
                stringRedisTemplate.rename(tmpKey, officialKey);
            } else {
                stringRedisTemplate.delete(officialKey);
            }
            stringRedisTemplate.expire(officialKey, Duration.ofSeconds(PostRedisKeys.TTL_SECONDS));
        } catch (Exception ex) {
            log.warn("重建热门池失败, bucket={}, error={}", bucket, ex.getMessage());
        }
    }

    public boolean hasMoreRecommend(GenderBucket bucket, int nextOffset) {
        return poolSize(PostRedisKeys.recommendPool(bucket)) > nextOffset;
    }

    public boolean hasMoreColdStart(GenderBucket bucket, int nextOffset) {
        return poolSize(PostRedisKeys.coldStartPool(bucket)) > nextOffset;
    }

    private List<Long> listZSetDesc(String key, int offset, int limit) {
        try {
            if (limit <= 0) {
                return List.of();
            }
            long start = offset;
            long end = offset + (long) limit - 1;
            Set<String> members = stringRedisTemplate.opsForZSet().reverseRange(key, start, end);
            if (members == null || members.isEmpty()) {
                return List.of();
            }
            List<Long> result = new ArrayList<>(members.size());
            for (String member : members) {
                result.add(Long.parseLong(member));
            }
            return result;
        } catch (Exception ex) {
            log.warn("读取 Feed 池失败, key={}, error={}", key, ex.getMessage());
            return List.of();
        }
    }

    private long poolSize(String key) {
        try {
            Long size = stringRedisTemplate.opsForZSet().size(key);
            return size == null ? 0L : size;
        } catch (Exception ex) {
            log.warn("读取池大小失败, key={}, error={}", key, ex.getMessage());
            return 0L;
        }
    }

    private void trimPool(String key, int maxSize) {
        Long size = stringRedisTemplate.opsForZSet().size(key);
        if (size != null && size > maxSize) {
            long removeCount = size - maxSize;
            stringRedisTemplate.opsForZSet().removeRange(key, 0, removeCount - 1);
        }
    }
}
