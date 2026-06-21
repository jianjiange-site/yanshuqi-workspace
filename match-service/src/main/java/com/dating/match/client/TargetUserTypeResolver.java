package com.dating.match.client;

/**
 * 目标用户类型解析，后续替换为 user-service gRPC 查询真实 user_type。
 */
public interface TargetUserTypeResolver {

    int resolveTargetUserType(long userId);
}
