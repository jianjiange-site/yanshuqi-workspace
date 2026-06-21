package com.dating.user.constant;

import com.dating.user.exception.UserBizException;
import com.dating.user.exception.UserErrorCode;

/**
 * Swagger 三方登录平台枚举，与 mobile-gateway 约定一致。
 */
public enum ThirdPartyPlatform {

    /** Google，platform=1。 */
    GOOGLE(1, IdentityType.GOOGLE, RegisterSource.GOOGLE),

    /** Apple，platform=2。 */
    APPLE(2, IdentityType.APPLE, RegisterSource.APPLE),

    /** Facebook，platform=3。 */
    FACEBOOK(3, IdentityType.FACEBOOK, RegisterSource.FACEBOOK);

    private final int platformCode;

    private final IdentityType identityType;

    private final RegisterSource registerSource;

    ThirdPartyPlatform(int platformCode, IdentityType identityType, RegisterSource registerSource) {
        this.platformCode = platformCode;
        this.identityType = identityType;
        this.registerSource = registerSource;
    }

    /**
     * 获取 Swagger 平台编码。
     *
     * @return 平台编码
     */
    public int getPlatformCode() {
        return platformCode;
    }

    /**
     * 获取对应登录凭证类型。
     *
     * @return 凭证类型
     */
    public IdentityType getIdentityType() {
        return identityType;
    }

    /**
     * 获取对应注册来源。
     *
     * @return 注册来源
     */
    public RegisterSource getRegisterSource() {
        return registerSource;
    }

    /**
     * 按 Swagger 平台编码解析三方平台。
     *
     * @param platformCode 平台编码
     * @return 三方平台枚举
     * @throws UserBizException 当编码非法时
     */
    public static ThirdPartyPlatform fromPlatformCode(int platformCode) {
        for (ThirdPartyPlatform platform : values()) {
            if (platform.platformCode == platformCode) {
                return platform;
            }
        }
        throw new UserBizException(UserErrorCode.INVALID_THIRD_PARTY_PLATFORM);
    }
}
