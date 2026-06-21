package com.dating.user.service.support;

import com.dating.user.constant.Gender;
import com.dating.user.dto.UpdateProfileCommand;
import com.dating.user.dto.UpsertOnboardingCommand;
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

    private static final int OCCUPATION_MAX_LENGTH = 128;

    private static final int EDUCATION_MAX_LENGTH = 128;

    private static final int LOCATION_MAX_LENGTH = 256;

    private static final int MAX_HEIGHT_CM = 300;

    private final ProfileBirthdayParser profileBirthdayParser;

    public ProfileFieldValidator(ProfileBirthdayParser profileBirthdayParser) {
        this.profileBirthdayParser = profileBirthdayParser;
    }

    /**
     * 校验并规范化更新资料命令中的字段。
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
        validateSwaggerNumericFields(command);
        command.setOccupation(normalizeOptionalText(command.getOccupation(), OCCUPATION_MAX_LENGTH, "职业"));
        command.setEducation(normalizeOptionalText(command.getEducation(), EDUCATION_MAX_LENGTH, "学历"));
        command.setLocation(normalizeOptionalText(command.getLocation(), LOCATION_MAX_LENGTH, "位置"));
    }

    /**
     * 校验并规范化 Onboarding 命令。
     */
    public void validateAndNormalizeOnboarding(UpsertOnboardingCommand command) {
        if (command == null || command.getUserId() == null || command.getUserId() <= 0) {
            throw new UserBizException(UserErrorCode.USER_REQUEST_INVALID, "用户 ID 非法");
        }
        command.setNickname(normalizeNickname(command.getNickname()));
        command.setGender(normalizeGenderStrict(command.getGender()));
        command.setBirthday(normalizeBirthdayString(command.getBirthday()));
        command.setAge(validateNonNegativeAge(command.getAge()));
        command.setHeight(validateNonNegativeHeight(command.getHeight()));
        command.setBio(normalizeBio(command.getBio()));
        command.setOccupation(normalizeOptionalText(command.getOccupation(), OCCUPATION_MAX_LENGTH, "职业"));
        command.setEducation(normalizeOptionalText(command.getEducation(), EDUCATION_MAX_LENGTH, "学历"));
        command.setLocation(normalizeOptionalText(command.getLocation(), LOCATION_MAX_LENGTH, "位置"));
        command.setDefaultAvatarObjectKey(normalizeOptionalText(
                command.getDefaultAvatarObjectKey(), 512, "默认头像 object key"));
    }

    /**
     * 解析 Onboarding birthday 为 LocalDate，兼容 yyyy-MM-dd / yyyy/MM/dd。
     */
    public LocalDate parseOnboardingBirthday(UpsertOnboardingCommand command) {
        if (command == null || !StringUtils.hasText(command.getBirthday())) {
            return null;
        }
        LocalDate birthDate = profileBirthdayParser.parse(command.getBirthday());
        validateBirthDateRange(birthDate);
        return birthDate;
    }

    private void validateSwaggerNumericFields(UpdateProfileCommand command) {
        if (command.isAgePresent()) {
            command.setAge(validateNonNegativeAge(command.getAge()));
        }
        if (command.isHeightPresent()) {
            command.setHeight(validateNonNegativeHeight(command.getHeight()));
        }
    }

    private String normalizeNickname(String nickname) {
        try {
            return normalizeOptionalText(nickname, NICKNAME_MAX_LENGTH, "昵称");
        } catch (UserBizException ex) {
            throw new UserBizException(UserErrorCode.INVALID_NICKNAME);
        }
    }

    private String normalizeBio(String bio) {
        try {
            return normalizeOptionalText(bio, BIO_MAX_LENGTH, "个人简介");
        } catch (UserBizException ex) {
            throw new UserBizException(UserErrorCode.INVALID_BIO);
        }
    }

    private String normalizeGenderStrict(String gender) {
        if (!StringUtils.hasText(gender)) {
            return null;
        }
        try {
            return Gender.valueOf(gender.trim().toUpperCase()).name();
        } catch (IllegalArgumentException ex) {
            throw new UserBizException(UserErrorCode.INVALID_GENDER);
        }
    }

    private String normalizeBirthdayString(String birthday) {
        if (!StringUtils.hasText(birthday)) {
            return null;
        }
        LocalDate parsed = profileBirthdayParser.parse(birthday);
        validateBirthDateRange(parsed);
        return profileBirthdayParser.format(parsed);
    }

    private Integer validateNonNegativeAge(Integer age) {
        if (age == null) {
            return null;
        }
        if (age < 0 || age > MAX_AGE) {
            throw new UserBizException(UserErrorCode.INVALID_AGE);
        }
        return age;
    }

    private Integer validateNonNegativeHeight(Integer height) {
        if (height == null) {
            return null;
        }
        if (height < 0 || height > MAX_HEIGHT_CM) {
            throw new UserBizException(UserErrorCode.INVALID_HEIGHT);
        }
        return height;
    }

    private void validateBirthDateRange(LocalDate birthDate) {
        if (birthDate == null) {
            return;
        }
        LocalDate today = LocalDate.now();
        if (birthDate.isAfter(today)) {
            throw new UserBizException(UserErrorCode.INVALID_BIRTHDAY);
        }
        int age = Period.between(birthDate, today).getYears();
        if (age < MIN_AGE || age > MAX_AGE) {
            throw new UserBizException(UserErrorCode.INVALID_BIRTHDAY);
        }
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
        validateBirthDateRange(birthDate);
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
