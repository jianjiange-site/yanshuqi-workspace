package com.dating.gateway.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "刷新令牌请求")
public class RefreshTokenReq {

    @NotBlank(message = "refreshToken 不能为空")
    private String refreshToken;

    @NotBlank(message = "deviceId 不能为空")
    private String deviceId;

    @Schema(description = "1=IOS, 2=ANDROID, 3=WEB")
    private Integer platform;

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public Integer getPlatform() {
        return platform;
    }

    public void setPlatform(Integer platform) {
        this.platform = platform;
    }
}
