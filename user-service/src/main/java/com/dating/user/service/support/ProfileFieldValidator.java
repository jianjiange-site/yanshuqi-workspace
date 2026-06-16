package com.dating.user.service.support;

import com.dating.user.constant.Gender;
import com.dating.user.dto.UpdateProfileCommand;
import com.dating.user.exception.UserBizException;
import com.dating.user.exception.UserErrorCode;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;

/**
 * 用户资料字段校验器。
 */
@Component
public class ProfileFieldValidator {

    private static final int NICKNAME_MAX_LENGTH = 64;

    private static final int COUNTRY_CODE_MAX_LENGTH = 16;

    private static final int CITY_CODE_MAX_LENGTH = 64;

    private static final int LANGUAGE_CODE_MAX_LENGTH = 16;

    private static final int LANGUAGE_CODES_MAX_SIZE = 10;

    private static final int BIO_MAX_LENGTH = 500;

    private static final int INTEREST_MAX_LENGTH = 32;

    private static final int INTERESTS_MAX_SIZE = 20;

    private static final int MIN_AGE = 18;

    private static final int MAX_AGE = 100;

    /**
     * 校验并规范化更新资料命令中的字段。
     *
     * @param command 更新资料命令
     * @throws UserBizException 当字段非法时
     */
    public void validateAndNormalize(UpdateProfileCommand command) {
        if (command == null || command.getUserId() == null || command.getUserId() <= 0) {
            throw new UserBizException(UserErrorCode.USER_REQUEST_INVALID, "用户 ID 非法");
        }
        command.setNickname(normalizeOptionalText(command.getNickname(), NICKNAME_MAX_LENGTH, "昵称"));
        command.setGender(normalizeGender(command.getGender()));
        command.setBirthDate(validateBirthDate(command.getBirthDate()));
        command.setCountryCode(normalizeOptionalText(command.getCountryCode(), COUNTRY_CODE_MAX_LENGTH, "国家编码"));
        command.setCityCode(normalizeOptionalText(command.getCityCode(), CITY_CODE_MAX_LENGTH, "城市编码"));
        command.setLanguageCodes(normalizeLanguageCodes(command.getLanguageCodes()));
        command.setBio(normalizeOptionalText(command.getBio(), BIO_MAX_LENGTH, "个人简介"));
        command.setInterests(normalizeInterests(command.getInterests()));
    }

    private String normalizeOptionalText(String value, int maxLength, String fieldName) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        String normalized = value.trim();
        if (normalized.length() > maxLength) {
            throw new UserBizException(UserErrorCode.PROFILE_UPDATE_INVALID, fieldName + "长度超限");
        }
        return normalized;
    }

    private String normalizeGender(String gender) {
        if (!StringUtils.hasText(gender)) {
            return null;
        }
        try {
            return Gender.valueOf(gender.trim().toUpperCase()).name();
        } catch (IllegalArgumentException ex) {
            throw new UserBizException(UserErrorCode.PROFILE_UPDATE_INVALID, "性别非法");
        }
    }

    private LocalDate validateBirthDate(LocalDate birthDate) {
        if (birthDate == null) {
            return null;
        }
        LocalDate today = LocalDate.now();
        if (birthDate.isAfter(today)) {
            throw new UserBizException(UserErrorCode.PROFILE_UPDATE_INVALID, "出生日期不能是未来日期");
        }
        int age = Period.between(birthDate, today).getYears();
        if (age < MIN_AGE || age > MAX_AGE) {
            throw new UserBizException(UserErrorCode.PROFILE_UPDATE_INVALID, "年龄需在 18 到 100 岁之间");
        }
        return birthDate;
    }

    private List<String> normalizeLanguageCodes(List<String> languageCodes) {
        if (languageCodes == null || languageCodes.isEmpty()) {
            return null;
        }
        if (languageCodes.size() > LANGUAGE_CODES_MAX_SIZE) {
            throw new UserBizException(UserErrorCode.PROFILE_UPDATE_INVALID, "语言列表数量超限");
        }
        List<String> normalized = new ArrayList<>();
        for (String code : languageCodes) {
            if (!StringUtils.hasText(code)) {
                continue;
            }
            String trimmed = code.trim();
            if (trimmed.length() > LANGUAGE_CODE_MAX_LENGTH) {
                throw new UserBizException(UserErrorCode.PROFILE_UPDATE_INVALID, "语言编码长度超限");
            }
            normalized.add(trimmed);
        }
        return normalized.isEmpty() ? null : normalized;
    }

    private List<String> normalizeInterests(List<String> interests) {
        if (interests == null || interests.isEmpty()) {
            return null;
        }
        if (interests.size() > INTERESTS_MAX_SIZE) {
            throw new UserBizException(UserErrorCode.PROFILE_UPDATE_INVALID, "兴趣标签数量超限");
        }
        List<String> normalized = new ArrayList<>();
        for (String interest : interests) {
            if (!StringUtils.hasText(interest)) {
                continue;
            }
            String trimmed = interest.trim();
            if (trimmed.length() > INTEREST_MAX_LENGTH) {
                throw new UserBizException(UserErrorCode.PROFILE_UPDATE_INVALID, "兴趣标签长度超限");
            }
            normalized.add(trimmed);
        }
        return normalized.isEmpty() ? null : normalized;
    }
}
