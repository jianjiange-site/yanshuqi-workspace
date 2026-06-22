package com.dating.post.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dating.post.entity.PostLikeEntity;
import org.apache.ibatis.annotations.Param;

/**
 * post_likes 单表 Mapper。
 */
public interface PostLikeMapper extends BaseMapper<PostLikeEntity> {

    /**
     * 幂等 upsert：仅当 status 发生变化时更新，返回受影响行数（0 表示重复操作）。
     */
    int upsertIfChanged(@Param("userId") long userId,
                        @Param("postId") long postId,
                        @Param("status") int status);

    Integer findStatus(@Param("userId") long userId, @Param("postId") long postId);
}
