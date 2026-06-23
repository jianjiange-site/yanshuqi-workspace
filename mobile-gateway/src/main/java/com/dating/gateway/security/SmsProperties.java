package com.dating.gateway.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 短信验证码配置；本阶段仅 mock，不接入真实短信供应商。
 */
@ConfigurationProperties(prefix = "gateway.sms")
public class SmsProperties {

    private boolean enabled = false;
    /** dev/test 联调固定验证码；prod 禁止在响应中返回。 */
    private String mockCode = "123456";
    private long codeTtlSeconds = 300L;
    private long cooldownSeconds = 60L;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getMockCode() {
        return mockCode;
    }

    public void setMockCode(String mockCode) {
        this.mockCode = mockCode;
    }

    public long getCodeTtlSeconds() {
        return codeTtlSeconds;
    }

    public void setCodeTtlSeconds(long codeTtlSeconds) {
        this.codeTtlSeconds = codeTtlSeconds;
    }

    public long getCooldownSeconds() {
        return cooldownSeconds;
    }

    public void setCooldownSeconds(long cooldownSeconds) {
        this.cooldownSeconds = cooldownSeconds;
    }
}
