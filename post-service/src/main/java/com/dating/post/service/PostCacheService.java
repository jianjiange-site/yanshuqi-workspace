package com.dating.post.service;

import com.dating.post.constant.PostRedisKeys;
import com.dating.post.dto.PostInfoDTO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;

/**
 * 帖子详情 Redis 缓存服务。
 * <p>
 * Redis 仅作读加速缓存，不是主存储；读写失败只记录 warn，不影响 DB 主流程。
 */
@Service
public class PostCacheService {

    private static final Logger log = LoggerFactory.getLogger(PostCacheService.class);

    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;

    public PostCacheService(StringRedisTemplate stringRedisTemplate, ObjectMapper objectMapper) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.objectMapper = objectMapper;
    }

    public Optional<PostInfoDTO> getDetail(long postId) {
        try {
            String json = stringRedisTemplate.opsForValue().get(PostRedisKeys.detail(postId));
            if (json == null || json.isBlank()) {
                return Optional.empty();
            }
            return Optional.of(objectMapper.readValue(json, PostInfoDTO.class));
        } catch (Exception ex) {
            log.warn("读取帖子详情缓存失败, postId={}, error={}", postId, ex.getMessage());
            return Optional.empty();
        }
    }

    public void putDetail(PostInfoDTO detail) {
        if (detail == null) {
            return;
        }
        try {
            String json = objectMapper.writeValueAsString(detail);
            stringRedisTemplate.opsForValue().set(
                    PostRedisKeys.detail(detail.getPostId()),
                    json,
                    Duration.ofSeconds(PostRedisKeys.DETAIL_TTL_SECONDS));
        } catch (JsonProcessingException ex) {
            log.warn("写入帖子详情缓存失败, postId={}, error={}", detail.getPostId(), ex.getMessage());
        } catch (Exception ex) {
            log.warn("写入帖子详情缓存失败, postId={}, error={}", detail.getPostId(), ex.getMessage());
        }
    }

    public void evictDetail(long postId) {
        try {
            stringRedisTemplate.delete(PostRedisKeys.detail(postId));
        } catch (Exception ex) {
            log.warn("删除帖子详情缓存失败, postId={}, error={}", postId, ex.getMessage());
        }
    }
}
