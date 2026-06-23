package com.dating.gateway.controller;

import com.dating.gateway.common.Result;
import com.dating.gateway.dto.LoginDeviceReq;
import com.dating.gateway.dto.LoginPhoneReq;
import com.dating.gateway.dto.LoginThirdPartyReq;
import com.dating.gateway.dto.RefreshTokenReq;
import com.dating.gateway.dto.SendSmsCodeReq;
import com.dating.gateway.dto.vo.LoginResultVO;
import com.dating.gateway.dto.vo.SendSmsCodeVO;
import com.dating.gateway.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Auth REST 入口：登录、刷新、登出、短信验证码。
 */
@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Auth", description = "登录、刷新、登出、短信验证码")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/send-sms-code")
    @Operation(summary = "发送短信验证码")
    public Result<SendSmsCodeVO> sendSmsCode(@Valid @RequestBody SendSmsCodeReq req) {
        return Result.ok(authService.sendSmsCode(req.getPhone()));
    }

    @PostMapping("/login-device")
    @Operation(summary = "设备匿名登录")
    public Result<LoginResultVO> loginDevice(@Valid @RequestBody LoginDeviceReq req) {
        return Result.ok(authService.loginDevice(req));
    }

    @PostMapping("/login-phone")
    @Operation(summary = "手机号验证码登录")
    public Result<LoginResultVO> loginPhone(@Valid @RequestBody LoginPhoneReq req) {
        return Result.ok(authService.loginPhone(req));
    }

    @PostMapping("/login-third-party")
    @Operation(summary = "三方登录")
    public Result<LoginResultVO> loginThirdParty(@Valid @RequestBody LoginThirdPartyReq req) {
        return Result.ok(authService.loginThirdParty(req));
    }

    @PostMapping("/refresh")
    @Operation(summary = "refresh token 轮换")
    public Result<LoginResultVO> refresh(@Valid @RequestBody RefreshTokenReq req) {
        return Result.ok(authService.refresh(req));
    }

    @PostMapping("/logout")
    @Operation(summary = "登出：access 拉黑 + refresh 撤销")
    public Result<Void> logout() {
        authService.logout();
        return Result.okVoid();
    }
}
