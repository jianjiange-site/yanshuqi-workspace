package com.dating.user.service;

import com.dating.user.dto.UpdateProfileCommand;
import com.dating.user.exception.UserBizException;
import com.dating.user.vo.UserProfileDetailVO;

/**
 * 用户资料业务服务。
 */
public interface UserProfileService {

    /**
     * 查询本人资料详情。
     *
     * @param userId 用户业务主键
     * @return 用户资料详情 VO
     * @throws UserBizException 当用户或资料不存在时
     */
    UserProfileDetailVO getSelfProfile(Long userId);

    /**
     * 更新本人基础资料，并计算完整度与 profile_status。
     *
     * @param command 更新资料命令
     * @return 更新后的用户资料详情 VO
     * @throws UserBizException 当用户不存在、账号状态非法或字段校验失败时
     */
    UserProfileDetailVO updateProfile(UpdateProfileCommand command);
}
