package com.dating.user.service;

import com.dating.user.vo.BasicUserProfileVO;
import com.dating.user.vo.RecommendUserProfileVO;
import com.dating.user.vo.UserAvailableVO;

import java.util.List;
import java.util.Map;

/**
 * 用户资料 Redis 缓存服务，cache-aside 读写。
 */
public interface UserProfileCacheService {

    /**
     * 批量读取基础资料缓存。
     *
     * @param userIds 用户业务主键列表
     * @return 命中的 userId 到 VO 映射
     */
    Map<Long, BasicUserProfileVO> getBasicProfiles(List<Long> userIds);

    /**
     * 批量写入基础资料缓存。
     *
     * @param profiles 基础资料 VO 映射
     */
    void putBasicProfiles(Map<Long, BasicUserProfileVO> profiles);

    /**
     * 批量读取推荐展示资料缓存。
     *
     * @param userIds 用户业务主键列表
     * @return 命中的 userId 到 VO 映射
     */
    Map<Long, RecommendUserProfileVO> getRecommendProfiles(List<Long> userIds);

    /**
     * 批量写入推荐展示资料缓存。
     *
     * @param profiles 推荐展示资料 VO 映射
     */
    void putRecommendProfiles(Map<Long, RecommendUserProfileVO> profiles);

    /**
     * 批量读取用户状态缓存。
     *
     * @param userIds 用户业务主键列表
     * @return 命中的 userId 到 VO 映射
     */
    Map<Long, UserAvailableVO> getUserStatuses(List<Long> userIds);

    /**
     * 批量写入用户状态缓存。
     *
     * @param statuses 用户可用性 VO 映射
     */
    void putUserStatuses(Map<Long, UserAvailableVO> statuses);
}
