package com.dating.user.vo;

/**
 * 绑定用户照片结果。
 */
public class BindPhotoResult {

    private UserPhotoVO photo;

    private String avatarKey;

    private String profileStatus;

    /**
     * 获取绑定后的照片信息。
     *
     * @return 照片 VO
     */
    public UserPhotoVO getPhoto() {
        return photo;
    }

    /**
     * 设置绑定后的照片信息。
     *
     * @param photo 照片 VO
     */
    public void setPhoto(UserPhotoVO photo) {
        this.photo = photo;
    }

    /**
     * 获取当前头像 object key。
     *
     * @return avatar_key，相册绑定时不更新则返回资料表当前值
     */
    public String getAvatarKey() {
        return avatarKey;
    }

    /**
     * 设置当前头像 object key。
     *
     * @param avatarKey avatar_key
     */
    public void setAvatarKey(String avatarKey) {
        this.avatarKey = avatarKey;
    }

    /**
     * 获取资料状态。
     *
     * @return profile_status
     */
    public String getProfileStatus() {
        return profileStatus;
    }

    /**
     * 设置资料状态。
     *
     * @param profileStatus profile_status
     */
    public void setProfileStatus(String profileStatus) {
        this.profileStatus = profileStatus;
    }
}
