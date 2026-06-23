package com.dating.gateway.service;

import com.dating.gateway.dto.LoginDeviceReq;
import com.dating.gateway.dto.LoginPhoneReq;
import com.dating.gateway.dto.LoginThirdPartyReq;
import com.dating.gateway.dto.RefreshTokenReq;
import com.dating.gateway.dto.vo.LoginResultVO;
import com.dating.gateway.dto.vo.SendSmsCodeVO;

/**
 * 网关鉴权域服务：登录、刷新、登出、短信验证码。
 */
public interface AuthService {

    SendSmsCodeVO sendSmsCode(String phone);

    LoginResultVO loginDevice(LoginDeviceReq req);

    LoginResultVO loginPhone(LoginPhoneReq req);

    LoginResultVO loginThirdParty(LoginThirdPartyReq req);

    LoginResultVO refresh(RefreshTokenReq req);

    void logout();
}
