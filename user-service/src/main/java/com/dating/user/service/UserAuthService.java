package com.dating.user.service;

import com.dating.user.dto.RegisterCommand;
import com.dating.user.exception.UserBizException;
import com.dating.user.vo.RegisterResult;

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
}
