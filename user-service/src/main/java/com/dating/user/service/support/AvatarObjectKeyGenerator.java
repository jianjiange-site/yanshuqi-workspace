package com.dating.user.service.support;

import com.dating.user.config.AvatarUploadProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.UUID;

/**
 * 头像 objectKey 生成器：avatar/{user_id}/{yyyyMM}/{uuid}.{ext}。
 */
@Component
public class AvatarObjectKeyGenerator {

    private static final DateTimeFormatter YEAR_MONTH = DateTimeFormatter.ofPattern("yyyyMM");

    private final AvatarUploadProperties avatarUploadProperties;

    public AvatarObjectKeyGenerator(AvatarUploadProperties avatarUploadProperties) {
        this.avatarUploadProperties = avatarUploadProperties;
    }

    /**
     * 生成服务端控制的 avatar objectKey，不允许客户端自定义完整路径。
     */
    public String generate(long userId, String normalizedExtWithoutDot) {
        String prefix = avatarUploadProperties.getObjectKeyPrefix();
        if (!StringUtils.hasText(prefix)) {
            prefix = "avatar";
        }
        String yyyyMm = YearMonth.now().format(YEAR_MONTH);
        String uuid = UUID.randomUUID().toString().replace("-", "");
        return prefix + "/" + userId + "/" + yyyyMm + "/" + uuid + "."
                + normalizedExtWithoutDot.toLowerCase(Locale.ROOT);
    }
}
