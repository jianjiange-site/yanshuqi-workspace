package com.dating.user.service.support;

import com.dating.user.exception.UserBizException;
import com.dating.user.exception.UserErrorCode;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * 生日字符串解析器，兼容 Swagger yyyy-MM-dd 与 yyyy/MM/dd。
 */
@Component
public class ProfileBirthdayParser {

    private static final DateTimeFormatter ISO = DateTimeFormatter.ISO_LOCAL_DATE;

    private static final DateTimeFormatter SLASH = DateTimeFormatter.ofPattern("yyyy/MM/dd");

    /**
     * 解析生日字符串为 LocalDate。
     *
     * @param birthday 生日字符串
     * @return 出生日期；空字符串返回 null
     * @throws UserBizException 当格式非法时
     */
    public LocalDate parse(String birthday) {
        if (!StringUtils.hasText(birthday)) {
            return null;
        }
        String trimmed = birthday.trim();
        try {
            if (trimmed.contains("/")) {
                return LocalDate.parse(trimmed, SLASH);
            }
            return LocalDate.parse(trimmed, ISO);
        } catch (DateTimeParseException ex) {
            throw new UserBizException(UserErrorCode.INVALID_BIRTHDAY);
        }
    }

    /**
     * 格式化出生日期为 yyyy-MM-dd。
     *
     * @param birthDate 出生日期
     * @return 格式化字符串；null 时返回 null
     */
    public String format(LocalDate birthDate) {
        return birthDate == null ? null : birthDate.format(ISO);
    }
}
