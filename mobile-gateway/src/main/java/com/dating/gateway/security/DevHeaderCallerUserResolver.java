package com.dating.gateway.security;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * 阶段 1 开发态 callerUserId 解析器。
 * <p>
 * 优先读取 Header {@code X-User-Id}；缺失时使用固定测试用户 ID。
 * 后续 JWT 阶段替换为 {@code JwtCallerUserResolver}，Controller 无需改动。
 */
@Component
public class DevHeaderCallerUserResolver implements CallerUserResolver {

    public static final String HEADER_USER_ID = "X-User-Id";

    /** 阶段 1 临时默认测试用户 ID，JWT 阶段不再使用。 */
    private static final long DEFAULT_TEST_USER_ID = 10001L;

    @Override
    public long resolveCallerUserId(HttpServletRequest request) {
        String headerValue = request.getHeader(HEADER_USER_ID);
        if (StringUtils.hasText(headerValue)) {
            try {
                long userId = Long.parseLong(headerValue.trim());
                if (userId > 0) {
                    return userId;
                }
            } catch (NumberFormatException ignored) {
                // 非法 Header 时回退默认测试用户
            }
        }
        return DEFAULT_TEST_USER_ID;
    }
}
