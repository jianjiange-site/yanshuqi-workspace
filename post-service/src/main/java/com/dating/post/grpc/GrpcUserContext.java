package com.dating.post.grpc;

/**
 * 保存当前 gRPC 请求上下文中的调用方 userId。
 * <p>
 * userId 由 {@link GrpcUserContextInterceptor} 从 Metadata {@code x-user-id} 写入，
 * 业务 RPC 通过 {@link #requireUserId()} 读取，而不是从 request body 获取。
 */
public final class GrpcUserContext {

    private static final ThreadLocal<Long> USER_ID = new ThreadLocal<>();

    private GrpcUserContext() {
    }

    public static void setUserId(Long userId) {
        if (userId == null) {
            USER_ID.remove();
            return;
        }
        USER_ID.set(userId);
    }

    public static Long getUserId() {
        return USER_ID.get();
    }

    /**
     * 读取调用方 userId；缺失时由上层抛出 {@link com.dating.post.exception.PostBusinessException}。
     */
    public static long requireUserId() {
        Long userId = USER_ID.get();
        if (userId == null || userId <= 0L) {
            throw new com.dating.post.exception.PostBusinessException(
                    com.dating.post.exception.PostErrorCode.UNAUTHORIZED);
        }
        return userId;
    }

    public static void clear() {
        USER_ID.remove();
    }
}
