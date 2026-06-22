package com.dating.post.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dating.post.entity.PostStatEntity;
import org.apache.ibatis.annotations.Param;

/**
 * post_stats 单表 Mapper。
 */
public interface PostStatMapper extends BaseMapper<PostStatEntity> {

    int addLikeCount(@Param("postId") long postId, @Param("delta") int delta);

    int addCommentCount(@Param("postId") long postId, @Param("delta") int delta);
}
