package com.dating.gateway.resolver;

/**
 * 从当前请求上下文解析 callerUserId，供 Controller 统一使用。
 */
public interface CallerUserResolver {

    /**
     * 解析当前登录用户 ID；无有效身份时抛出 {@link com.dating.gateway.exception.GatewayBizException}。
     */
    long resolveCallerUserId(jakarta.servlet.http.HttpServletRequest request);
}
