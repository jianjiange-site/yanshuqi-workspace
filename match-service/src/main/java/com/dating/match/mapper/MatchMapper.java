package com.dating.match.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dating.match.entity.MatchEntity;

import org.apache.ibatis.annotations.Param;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * 匹配关系 Mapper，仅访问 match 单表。
 */
public interface MatchMapper extends BaseMapper<MatchEntity> {

    List<MatchEntity> listByUserId(@Param("userId") Long userId,
                                   @Param("pageSize") int pageSize,
                                   @Param("cursorMatchedAt") OffsetDateTime cursorMatchedAt,
                                   @Param("cursorBizId") Long cursorBizId);
}
