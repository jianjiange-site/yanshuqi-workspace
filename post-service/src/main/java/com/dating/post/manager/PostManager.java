package com.dating.post.manager;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.dating.post.constant.PostStatus;
import com.dating.post.entity.PostEntity;
import com.dating.post.entity.PostImageEntity;
import com.dating.post.entity.PostStatEntity;
import com.dating.post.mapper.PostMapper;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * 帖子聚合 Manager：协调 posts / post_images / post_stats 单表读写，不做多表 JOIN。
 */
@Component
public class PostManager {

    private final PostMapper postMapper;
    private final PostImageManager postImageManager;
    private final PostStatManager postStatManager;

    public PostManager(PostMapper postMapper,
                       PostImageManager postImageManager,
                       PostStatManager postStatManager) {
        this.postMapper = postMapper;
        this.postImageManager = postImageManager;
        this.postStatManager = postStatManager;
    }

    /**
     * 事务内创建帖子主记录、图片与计数底座。
     */
    @Transactional
    public void createPost(long postId, long userId, String content, List<String> imageKeys) {
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);

        PostEntity post = new PostEntity();
        post.setPostId(postId);
        post.setUserId(userId);
        post.setContent(content);
        post.setStatus(PostStatus.ACTIVE);
        post.setCreatedAt(now);
        post.setUpdatedAt(now);
        post.setDeleted(0);
        postMapper.insert(post);

        List<PostImageEntity> images = new ArrayList<>();
        for (int i = 0; i < imageKeys.size(); i++) {
            PostImageEntity image = new PostImageEntity();
            image.setPostId(postId);
            image.setSortOrder(i);
            image.setImageKey(imageKeys.get(i));
            image.setCreatedAt(now);
            images.add(image);
        }
        if (!images.isEmpty()) {
            postImageManager.insertBatch(images);
        }

        postStatManager.insertInitial(postId);
    }

    public Optional<PostEntity> findActivePost(long postId) {
        LambdaQueryWrapper<PostEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PostEntity::getPostId, postId);
        return Optional.ofNullable(postMapper.selectOne(wrapper));
    }

    public List<PostEntity> listActivePostsByUser(long userId, long cursorPostId, int limit) {
        LambdaQueryWrapper<PostEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PostEntity::getUserId, userId);
        if (cursorPostId > 0L) {
            wrapper.lt(PostEntity::getPostId, cursorPostId);
        }
        wrapper.orderByDesc(PostEntity::getPostId)
                .last("LIMIT " + limit);
        return postMapper.selectList(wrapper);
    }

    /**
     * 查询近若干天内未删除、状态正常的帖子，供 Feed 热门池重建。
     */
    public List<PostEntity> listRecentActivePosts(OffsetDateTime since) {
        LambdaQueryWrapper<PostEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PostEntity::getStatus, PostStatus.ACTIVE)
                .ge(PostEntity::getCreatedAt, since)
                .orderByDesc(PostEntity::getPostId);
        return postMapper.selectList(wrapper);
    }

    /**
     * 逻辑删除帖子：保留图片与计数历史，供后续审计与阶段 3 互动数据关联。
     */
    @Transactional
    public void softDeletePost(PostEntity post) {
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        LambdaUpdateWrapper<PostEntity> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(PostEntity::getPostId, post.getPostId())
                .set(PostEntity::getDeleted, 1)
                .set(PostEntity::getStatus, PostStatus.INACTIVE)
                .set(PostEntity::getUpdatedAt, now);
        postMapper.update(null, wrapper);
    }

    public List<PostImageEntity> listImages(long postId) {
        return postImageManager.listByPostId(postId);
    }

    public Optional<PostStatEntity> findStat(long postId) {
        return postStatManager.findByPostId(postId);
    }
}
