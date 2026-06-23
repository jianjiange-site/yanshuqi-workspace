package com.dating.gateway.resolver;

/**
 * 开发态 Header 常量；dev/test 由 {@link com.dating.gateway.security.JwtAuthFilter} 兜底写入上下文。
 */
public final class DevHeaderCallerUserResolver {

    public static final String HEADER_USER_ID = "X-User-Id";

    private DevHeaderCallerUserResolver() {
    }
}
