package com.dating.user.vo;

/**
 * Swagger HomeCardVO 风格主页卡片视图。
 */
public class HomeCardProfileVO {

    private Long selfUserId;

    private UserProfileViewVO targetProfile;

    public Long getSelfUserId() {
        return selfUserId;
    }

    public void setSelfUserId(Long selfUserId) {
        this.selfUserId = selfUserId;
    }

    public UserProfileViewVO getTargetProfile() {
        return targetProfile;
    }

    public void setTargetProfile(UserProfileViewVO targetProfile) {
        this.targetProfile = targetProfile;
    }
}
