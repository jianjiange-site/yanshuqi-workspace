package com.dating.user.service.support;

import com.dating.user.constant.Gender;
import com.dating.user.constant.ProfileStatus;
import com.dating.user.dto.UpdateProfileCommand;
import com.dating.user.exception.UserBizException;
import com.dating.user.exception.UserErrorCode;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;

/**
 * 用户资料完整度计算器。
 */
@Component
public class ProfileCompletionCalculator {

    private static final int SCORE_THRESHOLD = 80;

    /**
     * 资料完整度计算结果。
     */
    public static final class CompletionResult {

        private final int profileScore;

        private final int profileCompleted;

        private final String profileStatus;

        /**
         * 构造完整度计算结果。
         *
         * @param profileScore     资料分数
         * @param profileCompleted 资料是否完成
         * @param profileStatus    资料状态
         */
        public CompletionResult(int profileScore, int profileCompleted, String profileStatus) {
            this.profileScore = profileScore;
            this.profileCompleted = profileCompleted;
            this.profileStatus = profileStatus;
        }

        /**
         * 获取资料分数。
         *
         * @return 资料分数
         */
        public int getProfileScore() {
            return profileScore;
        }

        /**
         * 获取资料是否完成。
         *
         * @return 0 或 1
         */
        public int getProfileCompleted() {
            return profileCompleted;
        }

        /**
         * 获取资料状态。
         *
         * @return 资料状态
         */
        public String getProfileStatus() {
            return profileStatus;
        }
    }

    /**
     * 根据更新命令计算 profile_score、profile_completed、profile_status。
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
        if (command.getBirthDate() != null) {
            score += 20;
        }
        if (StringUtils.hasText(command.getCountryCode()) && StringUtils.hasText(command.getCityCode())) {
            score += 20;
        }
        if (command.getInterests() != null && !command.getInterests().isEmpty()) {
            score += 20;
        }
        int profileCompleted = score >= SCORE_THRESHOLD ? 1 : 0;
        String profileStatus = score >= SCORE_THRESHOLD ? ProfileStatus.BASIC_DONE.name() : ProfileStatus.INIT.name();
        return new CompletionResult(score, profileCompleted, profileStatus);
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
