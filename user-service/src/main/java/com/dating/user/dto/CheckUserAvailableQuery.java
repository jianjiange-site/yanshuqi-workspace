package com.dating.user.dto;

import java.util.List;

/**
 * 批量检查用户可用性查询条件。
 */
public class CheckUserAvailableQuery {

    private List<Long> userIds;

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
}
