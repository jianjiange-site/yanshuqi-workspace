package com.dating.user.vo;

/**
 * 用户可用性 VO。
 */
public class UserAvailableVO {

    private Long userId;

    private boolean available;

    private String accountStatus;

    private String profileStatus;

    private String reason;

    /**
     * 获取用户业务主键。
     *
     * @return 用户业务主键
     */
    public Long getUserId() {
        return userId;
    }

    /**
     * 设置用户业务主键。
     *
     * @param userId 用户业务主键
     */
    public void setUserId(Long userId) {
        this.userId = userId;
    }

    /**
     * 是否可用。
     *
     * @return true 表示可用
     */
    public boolean isAvailable() {
        return available;
    }

    /**
     * 设置是否可用。
     *
     * @param available 是否可用
     */
    public void setAvailable(boolean available) {
        this.available = available;
    }

    /**
     * 获取账号状态。
     *
     * @return 账号状态
     */
    public String getAccountStatus() {
        return accountStatus;
    }

    /**
     * 设置账号状态。
     *
     * @param accountStatus 账号状态
     */
    public void setAccountStatus(String accountStatus) {
        this.accountStatus = accountStatus;
    }

    /**
     * 获取资料状态。
     *
     * @return 资料状态
     */
    public String getProfileStatus() {
        return profileStatus;
    }

    /**
     * 设置资料状态。
     *
     * @param profileStatus 资料状态
     */
    public void setProfileStatus(String profileStatus) {
        this.profileStatus = profileStatus;
    }

    /**
     * 获取不可用原因码。
     *
     * @return 不可用原因，可用时为空
     */
    public String getReason() {
        return reason;
    }

    /**
     * 设置不可用原因码。
     *
     * @param reason 不可用原因
     */
    public void setReason(String reason) {
        this.reason = reason;
    }
}
