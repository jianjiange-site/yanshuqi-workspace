package com.dating.match.client;

import com.dating.match.constant.UserTypeConstant;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;

/**
 * 目标用户类型 mock：默认 BH；测试可通过 {@link #setUserType(long, int)} 指定 DH。
 * 后续替换为 user-service gRPC。
 */
@Component
public class MockTargetUserTypeResolver implements TargetUserTypeResolver {

    private final ConcurrentHashMap<Long, Integer> overrides = new ConcurrentHashMap<>();

    @Override
    public int resolveTargetUserType(long userId) {
        return overrides.getOrDefault(userId, UserTypeConstant.BH);
    }

    public void setUserType(long userId, int userType) {
        overrides.put(userId, userType);
    }

    public void clear() {
        overrides.clear();
    }
}
