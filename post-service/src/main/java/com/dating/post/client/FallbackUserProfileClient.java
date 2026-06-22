package com.dating.post.client;

import com.dating.post.constant.GenderBucket;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * user-service 未接入前的临时降级实现。
 * <p>
 * 性别用 userId % 2 推断；好友列表返回空，写扩散自然为空，Feed 仅依赖热门池与冷启动池。
 * user-service 能力完整后替换为真实 gRPC Client，本类可删除或仅用于测试。
 */
@Component
public class FallbackUserProfileClient implements UserProfileClient {

    @Override
    public GenderBucket getGenderBucket(Long userId) {
        if (userId == null || userId <= 0L) {
            return GenderBucket.MALE;
        }
        return userId % 2 == 0 ? GenderBucket.FEMALE : GenderBucket.MALE;
    }

    @Override
    public Map<Long, GenderBucket> batchGetGenderBuckets(Collection<Long> userIds) {
        Map<Long, GenderBucket> result = new HashMap<>();
        if (userIds == null) {
            return result;
        }
        for (Long userId : userIds) {
            if (userId != null) {
                result.put(userId, getGenderBucket(userId));
            }
        }
        return result;
    }

    @Override
    public List<Long> getFriendUserIds(Long userId) {
        // 好友关系由 user-service 提供，本阶段不改 user-service，降级为空列表。
        return List.of();
    }
}
