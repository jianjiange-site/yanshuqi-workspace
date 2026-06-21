package com.dating.match.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dating.match.entity.ProfileVisitEntity;
import org.apache.ibatis.annotations.Param;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * 主页访问 Mapper，含 PostgreSQL UPSERT 自定义 SQL。
 */
public interface ProfileVisitMapper extends BaseMapper<ProfileVisitEntity> {

    /**
     * UPSERT 访问记录：首次插入，重复访问累加 visit_count。
     */
    void upsertVisit(@Param("bizId") Long bizId,
                     @Param("fromUserId") Long fromUserId,
                     @Param("targetUserId") Long targetUserId,
                     @Param("visitedAt") OffsetDateTime visitedAt);

    /**
     * 查询访问 target 主页的用户列表，按最近访问倒序。
     */
    List<ProfileVisitEntity> listVisitors(@Param("targetUserId") Long targetUserId,
                                          @Param("pageSize") int pageSize,
                                          @Param("cursorLastVisitedAt") OffsetDateTime cursorLastVisitedAt,
                                          @Param("cursorBizId") Long cursorBizId);
}
