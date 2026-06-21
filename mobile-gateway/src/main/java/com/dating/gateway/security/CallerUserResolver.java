package com.dating.gateway.security;

import jakarta.servlet.http.HttpServletRequest;

/**
 * 从请求上下文解析 callerUserId。
 * <p>
 * 阶段 1 使用开发态 Header 实现；JWT 阶段替换为从 access token 解析，接口保持不变。
 */
public interface CallerUserResolver {

    /**
     * 解析当前请求的发起用户业务 ID。
     *
     * @param request HTTP 请求
     * @return callerUserId
     */
    long resolveCallerUserId(HttpServletRequest request);
}
