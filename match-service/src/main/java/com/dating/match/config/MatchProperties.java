package com.dating.match.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Match 域配置：DH 延迟匹配随机延迟范围（毫秒）。
 */
@ConfigurationProperties(prefix = "app.match")
public class MatchProperties {

    /**
     * 生产默认 15 秒：给用户留出「未立即匹配」的产品感知。
     */
    private long dhDelayedMatchMinMs = 15000L;

    /**
     * 生产默认 2 分钟：避免 DH 匹配过于机械。
     */
    private long dhDelayedMatchMaxMs = 120000L;

    public long getDhDelayedMatchMinMs() {
        return dhDelayedMatchMinMs;
    }

    public void setDhDelayedMatchMinMs(long dhDelayedMatchMinMs) {
        this.dhDelayedMatchMinMs = dhDelayedMatchMinMs;
    }

    public long getDhDelayedMatchMaxMs() {
        return dhDelayedMatchMaxMs;
    }

    public void setDhDelayedMatchMaxMs(long dhDelayedMatchMaxMs) {
        this.dhDelayedMatchMaxMs = dhDelayedMatchMaxMs;
    }
}
