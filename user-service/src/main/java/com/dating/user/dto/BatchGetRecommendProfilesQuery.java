package com.dating.user.dto;

import java.util.List;

/**
 * 批量查询用户推荐展示资料查询条件。
 */
public class BatchGetRecommendProfilesQuery {

    private List<Long> userIds;

    private boolean includeUnavailable;

    /**
     * 获取用户业务主键列表。
     *
     * @return 用户 ID 列表
     */
    public List<Long> getUserIds() {
        return userIds;
    }

    /**
     * 设置用户业务主键列表。
     *
     * @param userIds 用户 ID 列表
     */
    public void setUserIds(List<Long> userIds) {
        this.userIds = userIds;
    }

    /**
     * 是否包含不可用用户。
     *
     * @return true 表示包含不可用用户
     */
    public boolean isIncludeUnavailable() {
        return includeUnavailable;
    }

    /**
     * 设置是否包含不可用用户。
     *
     * @param includeUnavailable 是否包含不可用用户
     */
    public void setIncludeUnavailable(boolean includeUnavailable) {
        this.includeUnavailable = includeUnavailable;
    }
}
