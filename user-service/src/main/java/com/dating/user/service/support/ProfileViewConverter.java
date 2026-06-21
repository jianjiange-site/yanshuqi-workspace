package com.dating.user.service.support;

import com.dating.user.entity.UserEntity;
import com.dating.user.entity.UserProfileEntity;
import com.dating.user.vo.AvatarViewVO;
import com.dating.user.vo.UserProfileViewVO;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * Swagger UserProfileVO 视图转换器。
 */
@Component
public class ProfileViewConverter {

    private final ProfileJsonSupport profileJsonSupport;
    private final ProfileBirthdayParser profileBirthdayParser;
    private final ProfileAgeResolver profileAgeResolver;
    private final LoginPendingCalculator loginPendingCalculator;

    private final AvatarViewConverter avatarViewConverter;

    public ProfileViewConverter(ProfileJsonSupport profileJsonSupport,
                                ProfileBirthdayParser profileBirthdayParser,
                                ProfileAgeResolver profileAgeResolver,
                                LoginPendingCalculator loginPendingCalculator,
                                AvatarViewConverter avatarViewConverter) {
        this.profileJsonSupport = profileJsonSupport;
        this.profileBirthdayParser = profileBirthdayParser;
        this.profileAgeResolver = profileAgeResolver;
        this.loginPendingCalculator = loginPendingCalculator;
        this.avatarViewConverter = avatarViewConverter;
    }

    /**
     * 组装 Swagger 风格资料视图。
     *
     * @param userEntity    用户主表
     * @param profileEntity 用户资料
     * @return 资料视图 VO
     */
    public UserProfileViewVO toView(UserEntity userEntity, UserProfileEntity profileEntity) {
        UserProfileViewVO view = new UserProfileViewVO();
        view.setUserId(userEntity.getUserId());
        view.setNickname(profileEntity.getNickname());
        view.setGender(profileEntity.getGender());
        view.setHeight(profileEntity.getHeight());
        view.setBio(profileEntity.getBio());
        view.setOccupation(profileEntity.getOccupation());
        view.setEducation(profileEntity.getEducation());
        view.setLocation(profileEntity.getLocation());
        view.setBirthday(profileBirthdayParser.format(profileEntity.getBirthDate()));
        view.setAge(profileAgeResolver.resolveDisplayAge(profileEntity));
        view.setInterests(profileJsonSupport.fromJsonArray(profileEntity.getInterests()));
        view.setAvatar(avatarViewConverter.fromObjectKey(profileEntity.getAvatarKey(), 0, 0));
        // pending 根据 profile_status / profile_completed 计算
        view.setPending(loginPendingCalculator.computePending(
                userEntity.getProfileStatus(), profileEntity.getProfileCompleted()));
        view.setRegulationStatus(profileEntity.getRegulationStatus() == null ? 0 : profileEntity.getRegulationStatus());
        view.setLastOpenAtMs(resolveLastOpenAtMs(userEntity, profileEntity));
        return view;
    }

    /**
     * lastOpenAtMs 优先取 last_open_at，否则回退 users.last_login_at。
     */
    private Long resolveLastOpenAtMs(UserEntity userEntity, UserProfileEntity profileEntity) {
        OffsetDateTime source = profileEntity.getLastOpenAt();
        if (source == null) {
            source = userEntity.getLastLoginAt();
        }
        return source == null ? null : source.toInstant().toEpochMilli();
    }
}
