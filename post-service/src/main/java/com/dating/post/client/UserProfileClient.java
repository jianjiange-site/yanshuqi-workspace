package com.dating.post.client;

import com.dating.post.constant.GenderBucket;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 用户资料 / 好友能力抽象，后续由 user-service gRPC 实现替换。
 */
public interface UserProfileClient {

    GenderBucket getGenderBucket(Long userId);

    Map<Long, GenderBucket> batchGetGenderBuckets(Collection<Long> userIds);

    List<Long> getFriendUserIds(Long userId);
}
