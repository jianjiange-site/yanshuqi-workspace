package com.dating.user.service.support;

import com.dating.user.constant.ProfileStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * 用户资料状态解析器，根据基础资料完成度与头像 key 计算 profile_status。
 */
@Component
public class ProfileStatusResolver {

    /**
     * 根据资料完成度与 avatar_key 解析 profile_status。
     *
     * @param profileCompleted 资料是否完成，0 或 1
     * @param avatarKey        头像 object key，可为 null
     * @return profile_status 枚举名
     */
    public String resolve(int profileCompleted, String avatarKey) {
        if (profileCompleted <= 0) {
            return ProfileStatus.INIT.name();
        }
        if (StringUtils.hasText(avatarKey)) {
            return ProfileStatus.PHOTO_DONE.name();
        }
        return ProfileStatus.BASIC_DONE.name();
    }
}
