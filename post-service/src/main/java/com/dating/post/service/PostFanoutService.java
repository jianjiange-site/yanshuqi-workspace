package com.dating.post.service;

import com.dating.post.client.UserProfileClient;
import com.dating.post.repository.UserTimelineRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 发帖后好友时间线写扩散。
 * <p>
 * 写扩散失败仅 warn、不回滚发帖：DB 已是事实源，timeline 为 best-effort 缓存层。
 */
@Service
public class PostFanoutService {

    private static final Logger log = LoggerFactory.getLogger(PostFanoutService.class);

    private final UserProfileClient userProfileClient;
    private final UserTimelineRepository userTimelineRepository;

    public PostFanoutService(UserProfileClient userProfileClient,
                             UserTimelineRepository userTimelineRepository) {
        this.userProfileClient = userProfileClient;
        this.userTimelineRepository = userTimelineRepository;
    }

    @Async
    public void fanoutToFriendsAsync(long authorUserId, long postId, long createdAtSeconds) {
        try {
            List<Long> friendIds = userProfileClient.getFriendUserIds(authorUserId);
            if (friendIds.isEmpty()) {
                return;
            }
            for (Long friendId : friendIds) {
                if (friendId != null && friendId > 0L) {
                    userTimelineRepository.addToTimeline(friendId, postId, createdAtSeconds);
                }
            }
            log.debug("写扩散完成, authorUserId={}, postId={}, friendCount={}", authorUserId, postId, friendIds.size());
        } catch (Exception ex) {
            log.warn("写扩散失败, authorUserId={}, postId={}, error={}", authorUserId, postId, ex.getMessage());
        }
    }
}
