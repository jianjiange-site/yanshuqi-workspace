package com.dating.user.service;

import com.dating.user.dto.UpdateProfileCommand;
import com.dating.user.dto.UpsertOnboardingCommand;
import com.dating.user.exception.UserBizException;
import com.dating.user.vo.UserProfileDetailVO;
import com.dating.user.vo.UserProfileViewVO;

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

    /**
     * 首次登录后补齐 onboarding 资料，返回 Swagger 风格视图。
     *
     * @param command Onboarding 命令
     * @return 资料视图
     * @throws UserBizException 当用户不存在、账号状态非法或字段校验失败时抛出
     */
    UserProfileViewVO upsertOnboarding(UpsertOnboardingCommand command);

    /**
     * 查询 Swagger 风格用户资料视图。
     *
     * @param userId 用户业务主键
     * @return 资料视图
     * @throws UserBizException 当用户或资料不存在时抛出
     */
    UserProfileViewVO getUserProfileView(Long userId);
}
