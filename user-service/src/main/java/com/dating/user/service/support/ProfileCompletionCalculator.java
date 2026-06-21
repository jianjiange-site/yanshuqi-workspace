package com.dating.user.service.support;

import com.dating.user.constant.Gender;
import com.dating.user.dto.UpdateProfileCommand;
import com.dating.user.entity.UserProfileEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.LocalDate;

/**
 * 用户资料完整度计算器。
 */
@Component
public class ProfileCompletionCalculator {

    private static final int SCORE_THRESHOLD = 80;

    private final ProfileJsonSupport profileJsonSupport;

    public ProfileCompletionCalculator(ProfileJsonSupport profileJsonSupport) {
        this.profileJsonSupport = profileJsonSupport;
    }

    /**
     * 资料完整度计算结果。
     */
    public static final class CompletionResult {

        private final int profileScore;

        private final int profileCompleted;

        public CompletionResult(int profileScore, int profileCompleted) {
            this.profileScore = profileScore;
            this.profileCompleted = profileCompleted;
        }

        public int getProfileScore() {
            return profileScore;
        }

        public int getProfileCompleted() {
            return profileCompleted;
        }
    }

    /**
     * 根据更新命令计算 profile_score、profile_completed（兼容 USER-01～08）。
     *
     * @param command 更新资料命令
     * @return 完整度计算结果
     */
    public CompletionResult calculate(UpdateProfileCommand command) {
        int score = 0;
        if (StringUtils.hasText(command.getNickname())) {
            score += 20;
        }
        if (hasValidGender(command.getGender())) {
            score += 20;
        }
        if (command.getBirthDate() != null || (command.getAge() != null && command.getAge() > 0)) {
            score += 20;
        }
        if (hasRegion(command.getCountryCode(), command.getCityCode(), command.getLocation())) {
            score += 20;
        }
        if (command.getInterests() != null && !command.getInterests().isEmpty()) {
            score += 20;
        }
        return toResult(score);
    }

    /**
     * 根据资料实体重新计算完整度，供 Onboarding / Update 落库后使用。
     *
     * @param profileEntity 用户资料实体
     * @return 完整度计算结果
     */
    public CompletionResult calculateFromEntity(UserProfileEntity profileEntity) {
        int score = 0;
        if (StringUtils.hasText(profileEntity.getNickname())) {
            score += 15;
        }
        if (hasValidGender(profileEntity.getGender())) {
            score += 15;
        }
        if (profileEntity.getBirthDate() != null
                || (profileEntity.getAge() != null && profileEntity.getAge() > 0)) {
            score += 15;
        }
        if (hasRegion(profileEntity.getCountryCode(), profileEntity.getCityCode(), profileEntity.getLocation())) {
            score += 15;
        }
        if (StringUtils.hasText(profileEntity.getBio())) {
            score += 10;
        }
        if (profileEntity.getHeight() != null && profileEntity.getHeight() > 0) {
            score += 10;
        }
        if (StringUtils.hasText(profileEntity.getOccupation())) {
            score += 10;
        }
        if (StringUtils.hasText(profileEntity.getEducation())) {
            score += 10;
        }
        if (profileJsonSupport.fromJsonArray(profileEntity.getInterests()) != null
                && !profileJsonSupport.fromJsonArray(profileEntity.getInterests()).isEmpty()) {
            score += 10;
        }
        return toResult(score);
    }

    private CompletionResult toResult(int score) {
        int profileCompleted = score >= SCORE_THRESHOLD ? 1 : 0;
        return new CompletionResult(score, profileCompleted);
    }

    private boolean hasRegion(String countryCode, String cityCode, String location) {
        if (StringUtils.hasText(location)) {
            return true;
        }
        return StringUtils.hasText(countryCode) && StringUtils.hasText(cityCode);
    }

    private boolean hasValidGender(String gender) {
        if (!StringUtils.hasText(gender)) {
            return false;
        }
        try {
            Gender.valueOf(gender.trim().toUpperCase());
            return true;
        } catch (IllegalArgumentException ex) {
            return false;
        }
    }
}
