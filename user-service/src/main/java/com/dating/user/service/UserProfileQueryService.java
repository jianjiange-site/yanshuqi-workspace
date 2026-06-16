package com.dating.user.service;

import com.dating.user.dto.BatchGetBasicProfilesQuery;
import com.dating.user.dto.BatchGetRecommendProfilesQuery;
import com.dating.user.dto.CheckUserAvailableQuery;
import com.dating.user.vo.BasicUserProfileVO;
import com.dating.user.vo.RecommendUserProfileVO;
import com.dating.user.vo.UserAvailableVO;

import java.util.List;

/**
 * 用户资料批量只读查询服务。
 */
public interface UserProfileQueryService {

    /**
     * 批量查询用户基础资料。
     *
     * @param query 查询条件
     * @return 基础资料 VO 列表
     */
    List<BasicUserProfileVO> batchGetBasicProfiles(BatchGetBasicProfilesQuery query);

    /**
     * 批量查询用户推荐展示资料，不做推荐算法。
     *
     * @param query 查询条件
     * @return 推荐展示资料 VO 列表
     */
    List<RecommendUserProfileVO> batchGetRecommendProfiles(BatchGetRecommendProfilesQuery query);

    /**
     * 批量检查用户是否可用。
     *
     * @param query 查询条件
     * @return 用户可用性 VO 列表
     */
    List<UserAvailableVO> checkUserAvailable(CheckUserAvailableQuery query);
}
