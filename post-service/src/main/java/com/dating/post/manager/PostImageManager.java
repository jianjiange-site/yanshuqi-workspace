package com.dating.post.manager;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.dating.post.entity.PostImageEntity;
import com.dating.post.mapper.PostImageMapper;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 帖子图片 Manager，仅访问 post_images 单表。
 */
@Component
public class PostImageManager {

    private final PostImageMapper postImageMapper;

    public PostImageManager(PostImageMapper postImageMapper) {
        this.postImageMapper = postImageMapper;
    }

    public void insertBatch(List<PostImageEntity> images) {
        for (PostImageEntity image : images) {
            postImageMapper.insertOne(image);
        }
    }

    public List<PostImageEntity> listByPostId(long postId) {
        LambdaQueryWrapper<PostImageEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PostImageEntity::getPostId, postId)
                .orderByAsc(PostImageEntity::getSortOrder);
        return postImageMapper.selectList(wrapper);
    }
}
