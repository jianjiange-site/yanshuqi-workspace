package com.dating.post.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dating.post.entity.PostCommentEntity;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * post_comments 单表 Mapper。
 */
public interface PostCommentMapper extends BaseMapper<PostCommentEntity> {

    PostCommentEntity findByCommentId(@Param("commentId") long commentId);

    List<PostCommentEntity> listByPostIdBeforeCommentId(@Param("postId") long postId,
                                                        @Param("cursorCommentId") long cursorCommentId,
                                                        @Param("limit") int limit);
}
