package com.dating.post.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dating.post.entity.PostImageEntity;
import org.apache.ibatis.annotations.Insert;

/**
 * post_images 单表 Mapper。
 */
public interface PostImageMapper extends BaseMapper<PostImageEntity> {

    @Insert("""
            INSERT INTO post_images (post_id, sort_order, image_key, created_at)
            VALUES (#{postId}, #{sortOrder}, #{imageKey}, #{createdAt})
            """)
    int insertOne(PostImageEntity entity);
}
