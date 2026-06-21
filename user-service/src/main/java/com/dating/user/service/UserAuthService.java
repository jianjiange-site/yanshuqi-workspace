package com.dating.user.service;

import com.dating.user.dto.LoginCommand;
import com.dating.user.dto.RegisterCommand;
import com.dating.user.dto.ResolveOrCreateDeviceUserCommand;
import com.dating.user.dto.ResolveOrCreatePhoneUserCommand;
import com.dating.user.dto.ResolveOrCreateThirdPartyUserCommand;
import com.dating.user.exception.UserBizException;
import com.dating.user.vo.LoginResult;
import com.dating.user.vo.RegisterResult;
import com.dating.user.vo.ResolveOrCreateLoginUserResult;

/**
 * 用户认证业务服务。
 */
public interface UserAuthService {

    /**
     * 注册新用户，并初始化 users、auth_identities、profiles、settings 四张表基础数据。
     *
     * @param command 注册命令，包含登录凭证、用户类型和注册来源
     * @return 注册结果，仅包含 userId、accountStatus、profileStatus、tokenVersion
     * @throws UserBizException 当凭证已存在、参数非法或数据库写入失败时抛出
     */
    RegisterResult register(RegisterCommand command);

    /**
     * 校验登录凭证与密码，更新设备与最近登录时间，不签发 JWT。
     *
     * @param command 登录命令，包含凭证、密码与设备信息
     * @return 登录结果，供 gateway 签发 JWT
     * @throws UserBizException 当凭证不存在、密码错误、账号状态非法或写入失败时抛出
     */
    LoginResult verifyLogin(LoginCommand command);

    /**
     * 设备匿名登录：解析或创建设备身份用户，不签发 JWT。
     *
     * @param command 设备登录命令
     * @return 解析或创建结果
     * @throws UserBizException 当参数非法、账号状态非法或写入失败时抛出
     */
    ResolveOrCreateLoginUserResult resolveOrCreateDeviceUser(ResolveOrCreateDeviceUserCommand command);

    /**
     * 手机号登录：解析或创建手机号身份用户，不校验真实短信，不签发 JWT。
     *
     * @param command 手机号登录命令
     * @return 解析或创建结果
     * @throws UserBizException 当参数非法、账号状态非法或写入失败时抛出
     */
    ResolveOrCreateLoginUserResult resolveOrCreatePhoneUser(ResolveOrCreatePhoneUserCommand command);

    /**
     * 三方登录：解析或创建三方身份用户，不校验真实 OAuth，不签发 JWT。
     *
     * @param command 三方登录命令
     * @return 解析或创建结果
     * @throws UserBizException 当参数非法、账号状态非法或写入失败时抛出
     */
    ResolveOrCreateLoginUserResult resolveOrCreateThirdPartyUser(ResolveOrCreateThirdPartyUserCommand command);
}
