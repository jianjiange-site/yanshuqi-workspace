package com.dating.user.dto;

/**
 * 主页卡片查询命令。
 */
public class GetHomeCardProfileQuery {

    private Long selfUserId;

    private Long targetUserId;

    public Long getSelfUserId() {
        return selfUserId;
    }

    public void setSelfUserId(Long selfUserId) {
        this.selfUserId = selfUserId;
    }

    public Long getTargetUserId() {
        return targetUserId;
    }

    public void setTargetUserId(Long targetUserId) {
        this.targetUserId = targetUserId;
    }
}
