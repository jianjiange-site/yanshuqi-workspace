package com.dating.user.dto;

/**
 * 用户注册命令，作为 Service 层入参，不直接使用 gRPC Request。
 */
public class RegisterCommand {

    private String identityType;

    private String identityValue;

    private String password;

    private String userType;

    private String registerSource;

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
     * 获取注册密码明文，仅用于哈希，禁止写入日志。
     *
     * @return 注册密码
     */
    public String getPassword() {
        return password;
    }

    /**
     * 设置注册密码明文。
     *
     * @param password 注册密码
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * 获取用户类型。
     *
     * @return 用户类型，本阶段仅支持 BH
     */
    public String getUserType() {
        return userType;
    }

    /**
     * 设置用户类型。
     *
     * @param userType 用户类型
     */
    public void setUserType(String userType) {
        this.userType = userType;
    }

    /**
     * 获取注册来源。
     *
     * @return 注册来源
     */
    public String getRegisterSource() {
        return registerSource;
    }

    /**
     * 设置注册来源。
     *
     * @param registerSource 注册来源
     */
    public void setRegisterSource(String registerSource) {
        this.registerSource = registerSource;
    }
}
