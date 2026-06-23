package com.dating.gateway.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "三方登录请求")
public class LoginThirdPartyReq {

    @NotNull(message = "thirdPartyPlatform 不能为空")
    @Schema(description = "1=Google, 2=Apple, 3=Facebook")
    private Integer thirdPartyPlatform;

    @NotBlank(message = "idToken 不能为空")
    private String idToken;

    private String googleEmail;

    @NotBlank(message = "deviceId 不能为空")
    private String deviceId;

    @NotNull(message = "platform 不能为空")
    private Integer platform;

    private String deviceModel;
    private String osVersion;
    private String appVersion;
    private String pushToken;

    public Integer getThirdPartyPlatform() {
        return thirdPartyPlatform;
    }

    public void setThirdPartyPlatform(Integer thirdPartyPlatform) {
        this.thirdPartyPlatform = thirdPartyPlatform;
    }

    public String getIdToken() {
        return idToken;
    }

    public void setIdToken(String idToken) {
        this.idToken = idToken;
    }

    public String getGoogleEmail() {
        return googleEmail;
    }

    public void setGoogleEmail(String googleEmail) {
        this.googleEmail = googleEmail;
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

    public String getDeviceModel() {
        return deviceModel;
    }

    public void setDeviceModel(String deviceModel) {
        this.deviceModel = deviceModel;
    }

    public String getOsVersion() {
        return osVersion;
    }

    public void setOsVersion(String osVersion) {
        this.osVersion = osVersion;
    }

    public String getAppVersion() {
        return appVersion;
    }

    public void setAppVersion(String appVersion) {
        this.appVersion = appVersion;
    }

    public String getPushToken() {
        return pushToken;
    }

    public void setPushToken(String pushToken) {
        this.pushToken = pushToken;
    }
}
