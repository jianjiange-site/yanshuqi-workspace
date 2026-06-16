package com.dating.user.dto;

/**
 * 设备信息命令，作为登录等场景的 Service 层入参组成部分。
 */
public class DeviceInfoCommand {

    private String platform;

    private String deviceFingerprint;

    private String pushToken;

    private String appVersion;

    /**
     * 获取设备平台。
     *
     * @return 平台，如 IOS、ANDROID、WEB
     */
    public String getPlatform() {
        return platform;
    }

    /**
     * 设置设备平台。
     *
     * @param platform 平台
     */
    public void setPlatform(String platform) {
        this.platform = platform;
    }

    /**
     * 获取设备指纹，禁止写入日志明文。
     *
     * @return 设备指纹
     */
    public String getDeviceFingerprint() {
        return deviceFingerprint;
    }

    /**
     * 设置设备指纹。
     *
     * @param deviceFingerprint 设备指纹
     */
    public void setDeviceFingerprint(String deviceFingerprint) {
        this.deviceFingerprint = deviceFingerprint;
    }

    /**
     * 获取推送 token 明文，仅用于哈希，禁止写入日志。
     *
     * @return 推送 token
     */
    public String getPushToken() {
        return pushToken;
    }

    /**
     * 设置推送 token。
     *
     * @param pushToken 推送 token
     */
    public void setPushToken(String pushToken) {
        this.pushToken = pushToken;
    }

    /**
     * 获取 App 版本。
     *
     * @return App 版本
     */
    public String getAppVersion() {
        return appVersion;
    }

    /**
     * 设置 App 版本。
     *
     * @param appVersion App 版本
     */
    public void setAppVersion(String appVersion) {
        this.appVersion = appVersion;
    }
}
