package com.dating.user.dto;

/**
 * 用户登录校验命令，作为 Service 层入参，不直接使用 gRPC Request。
 */
public class LoginCommand {

    private String identityType;

    private String identityValue;

    private String password;

    private DeviceInfoCommand deviceInfo;

    /**
     * 获取登录凭证类型。
     *
     * @return 凭证类型，如 PHONE、EMAIL、DEVICE
     */
    public String getIdentityType() {
        return identityType;
    }

    /**
     * 设置登录凭证类型。
     *
     * @param identityType 凭证类型
     */
    public void setIdentityType(String identityType) {
        this.identityType = identityType;
    }

    /**
     * 获取登录凭证原始值。
     *
     * @return 凭证原始值
     */
    public String getIdentityValue() {
        return identityValue;
    }

    /**
     * 设置登录凭证原始值。
     *
     * @param identityValue 凭证原始值
     */
    public void setIdentityValue(String identityValue) {
        this.identityValue = identityValue;
    }

    /**
     * 获取登录密码明文，仅用于校验，禁止写入日志。
     *
     * @return 登录密码
     */
    public String getPassword() {
        return password;
    }

    /**
     * 设置登录密码明文。
     *
     * @param password 登录密码
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * 获取设备信息。
     *
     * @return 设备信息命令
     */
    public DeviceInfoCommand getDeviceInfo() {
        return deviceInfo;
    }

    /**
     * 设置设备信息。
     *
     * @param deviceInfo 设备信息命令
     */
    public void setDeviceInfo(DeviceInfoCommand deviceInfo) {
        this.deviceInfo = deviceInfo;
    }
}
