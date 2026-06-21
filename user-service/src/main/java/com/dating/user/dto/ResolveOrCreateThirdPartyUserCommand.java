package com.dating.user.dto;

/**
 * 三方登录命令。
 */
public class ResolveOrCreateThirdPartyUserCommand {

    private int thirdPartyPlatform;

    private String idToken;

    private String googleEmail;

    private String deviceId;

    private String platform;

    private String deviceModel;

    private String osVersion;

    private String appVersion;

    private String pushToken;

    public int getThirdPartyPlatform() {
        return thirdPartyPlatform;
    }

    public void setThirdPartyPlatform(int thirdPartyPlatform) {
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

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
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
