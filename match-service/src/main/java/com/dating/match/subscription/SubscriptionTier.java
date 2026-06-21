package com.dating.match.subscription;

/**
 * 订阅档位枚举，本阶段 mock 查询，后续接 payment-service。
 */
public enum SubscriptionTier {

    FREE("FREE"),
    WEEKLY("WEEKLY"),
    MONTHLY("MONTHLY"),
    YEARLY("YEARLY");

    private final String code;

    SubscriptionTier(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public QuotaLimit quotaLimit() {
        return switch (this) {
            case FREE -> new QuotaLimit(5, 50, 0);
            case WEEKLY -> new QuotaLimit(10, 80, 0);
            case MONTHLY, YEARLY -> new QuotaLimit(15, 120, 1);
        };
    }
}
