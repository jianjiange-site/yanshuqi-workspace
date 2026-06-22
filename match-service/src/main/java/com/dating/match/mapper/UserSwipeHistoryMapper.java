package com.dating.match.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dating.match.entity.UserSwipeHistoryEntity;
import org.apache.ibatis.annotations.Param;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * 划卡历史 Mapper，仅访问 user_swipe_history 单表。
 */
public interface UserSwipeHistoryMapper extends BaseMapper<UserSwipeHistoryEntity> {

    List<Long> listPositiveTargetIds(@Param("userId") Long userId,
                                     @Param("since") OffsetDateTime since,
                                     @Param("limit") int limit);

    List<Long> listAllSwipedTargetIds(@Param("userId") Long userId,
                                      @Param("limit") int limit);

    int countSwipeBetween(@Param("userId") Long userId,
                          @Param("start") OffsetDateTime start,
                          @Param("end") OffsetDateTime end);

    List<Long> listUsersWithSwipeBetween(@Param("start") OffsetDateTime start,
                                         @Param("end") OffsetDateTime end,
                                         @Param("limit") int limit);
}
