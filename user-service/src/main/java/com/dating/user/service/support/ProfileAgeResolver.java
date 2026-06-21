package com.dating.user.service.support;

import com.dating.user.entity.UserProfileEntity;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.Period;

/**
 * 年龄解析器：优先由 birthday 推导，缺失时使用落库 age。
 */
@Component
public class ProfileAgeResolver {

    private final ProfileBirthdayParser profileBirthdayParser;

    public ProfileAgeResolver(ProfileBirthdayParser profileBirthdayParser) {
        this.profileBirthdayParser = profileBirthdayParser;
    }

    /**
     * 解析展示用年龄。
     *
     * @param profileEntity 用户资料实体
     * @return 年龄；无法解析时返回 null
     */
    public Integer resolveDisplayAge(UserProfileEntity profileEntity) {
        if (profileEntity == null) {
            return null;
        }
        // birthday 存在时优先推导，避免与落库 age 冲突
        if (profileEntity.getBirthDate() != null) {
            return Period.between(profileEntity.getBirthDate(), LocalDate.now()).getYears();
        }
        return profileEntity.getAge();
    }
}
