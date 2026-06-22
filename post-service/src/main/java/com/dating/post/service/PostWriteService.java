package com.dating.post.service;

import com.dating.post.client.UserProfileClient;
import com.dating.post.constant.GenderBucket;
import com.dating.post.dto.PostCreateCommand;
import com.dating.post.dto.PostInfoDTO;
import com.dating.post.entity.PostEntity;
import com.dating.post.exception.PostBusinessException;
import com.dating.post.exception.PostErrorCode;
import com.dating.post.manager.PostManager;
import com.dating.post.repository.FeedPoolRepository;
import com.dating.post.service.support.BusinessIdGenerator;
import org.springframework.stereotype.Service;

/**
 * 帖子写业务：发帖与删帖。
 */
@Service
public class PostWriteService {

    private final PostManager postManager;
    private final PostReadService postReadService;
    private final PostCacheService postCacheService;
    private final BusinessIdGenerator businessIdGenerator;
    private final UserProfileClient userProfileClient;
    private final FeedPoolRepository feedPoolRepository;
    private final PostFanoutService postFanoutService;

    public PostWriteService(PostManager postManager,
                            PostReadService postReadService,
                            PostCacheService postCacheService,
                            BusinessIdGenerator businessIdGenerator,
                            UserProfileClient userProfileClient,
                            FeedPoolRepository feedPoolRepository,
                            PostFanoutService postFanoutService) {
        this.postManager = postManager;
        this.postReadService = postReadService;
        this.postCacheService = postCacheService;
        this.businessIdGenerator = businessIdGenerator;
        this.userProfileClient = userProfileClient;
        this.feedPoolRepository = feedPoolRepository;
        this.postFanoutService = postFanoutService;
    }

    /**
     * 创建帖子。
     * <p>
     * DB 写入在 {@link PostManager#createPost} 事务内完成；Redis 缓存在事务提交后 best-effort 写入，
     * 缓存失败不回滚 DB，保证 PostgreSQL 始终是最终事实源。
     */
    public long createPost(PostCreateCommand command) {
        long postId = businessIdGenerator.nextId();
        postManager.createPost(
                postId,
                command.getCallerUserId(),
                command.getContent(),
                command.getImageKeys());

        PostInfoDTO detail = postReadService.loadFromDb(postId)
                .orElseThrow(() -> new PostBusinessException(PostErrorCode.INTERNAL_ERROR, "发帖后回读失败"));
        postCacheService.putDetail(detail);

        // 事务外 best-effort：冷启动池 + 好友写扩散，失败不回滚 DB。
        GenderBucket authorGender = userProfileClient.getGenderBucket(command.getCallerUserId());
        feedPoolRepository.addToColdStartPool(authorGender, postId, detail.getCreatedAtSeconds());
        postFanoutService.fanoutToFriendsAsync(command.getCallerUserId(), postId, detail.getCreatedAtSeconds());
        return postId;
    }

    /**
     * 删除帖子（逻辑删除）。
     * <p>
     * 采用 deleted=1 + status=0 软删，不物理删除 post_images / post_stats，
     * 以便阶段 3 点赞评论历史仍可关联，并支持后续审计与补偿。
     */
    public void deletePost(long callerUserId, long postId) {
        if (postId <= 0L) {
            throw new PostBusinessException(PostErrorCode.INVALID_ARGUMENT, "postId 非法");
        }
        PostEntity post = postManager.findActivePost(postId)
                .orElseThrow(() -> new PostBusinessException(PostErrorCode.POST_NOT_FOUND));
        if (post.getUserId() == null || post.getUserId() != callerUserId) {
            throw new PostBusinessException(PostErrorCode.FORBIDDEN, "仅作者可删除帖子");
        }

        postManager.softDeletePost(post);
        postCacheService.evictDetail(postId);
    }
}
