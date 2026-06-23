package com.dating.gateway.security;

/**
 * 当前请求鉴权上下文（ThreadLocal），由 {@link JwtAuthFilter} 写入并在 finally 中清理。
 */
public final class CurrentUserContext {

    private static final ThreadLocal<JwtClaims> HOLDER = new ThreadLocal<>();

    private CurrentUserContext() {
    }

    public static void set(JwtClaims claims) {
        HOLDER.set(claims);
    }

    public static JwtClaims get() {
        return HOLDER.get();
    }

    public static void clear() {
        HOLDER.remove();
    }

    public static long requireUserId() {
        JwtClaims claims = HOLDER.get();
        if (claims == null) {
            return 0L;
        }
        return claims.getUserId();
    }
}
