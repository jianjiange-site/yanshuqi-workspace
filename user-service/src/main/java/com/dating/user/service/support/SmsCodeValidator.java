package com.dating.user.service.support;

import com.dating.user.exception.UserBizException;
import com.dating.user.exception.UserErrorCode;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * 短信验证码校验入口，本阶段不做真实短信通道校验。
 */
@Component
public class SmsCodeValidator {

    /**
     * 校验短信验证码格式，预留真实短信校验扩展点。
     *
     * @param smsCode 短信验证码，禁止写入日志
     * @throws UserBizException 当验证码为空时
     */
    public void validate(String smsCode) {
        if (!StringUtils.hasText(smsCode)) {
            throw new UserBizException(UserErrorCode.INVALID_SMS_CODE);
        }
        // 本阶段不做真实短信校验，后续可接入短信服务或 gateway 预校验结果
    }
}
