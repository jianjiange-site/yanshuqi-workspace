package com.dating.user.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 用户域缓存与慢调用配置。
 */
@ConfigurationProperties(prefix = "app.user-cache")
public class UserCacheProperties {

    /** 资料缓存 TTL（秒），默认 600 秒。 */
    private int profileTtlSeconds = 600;

    /** 慢调用阈值（毫秒），默认 500 毫秒。 */
    private int slowCallThresholdMs = 500;

    /**
     * 获取资料缓存 TTL（秒）。
     *
     * @return TTL 秒数
     */
    public int getProfileTtlSeconds() {
        return profileTtlSeconds;
    }

    /**
     * 设置资料缓存 TTL（秒）。
     *
     * @param profileTtlSeconds TTL 秒数
     */
    public void setProfileTtlSeconds(int profileTtlSeconds) {
        this.profileTtlSeconds = profileTtlSeconds;
    }

    /**
     * 获取慢调用阈值（毫秒）。
     *
     * @return 阈值毫秒数
     */
    public int getSlowCallThresholdMs() {
        return slowCallThresholdMs;
    }

    /**
     * 设置慢调用阈值（毫秒）。
     *
     * @param slowCallThresholdMs 阈值毫秒数
     */
    public void setSlowCallThresholdMs(int slowCallThresholdMs) {
        this.slowCallThresholdMs = slowCallThresholdMs;
    }
}
