package com.dating.user.service;

import com.dating.user.dto.GetHomeCardProfileQuery;
import com.dating.user.exception.UserBizException;
import com.dating.user.vo.HomeCardProfileVO;

/**
 * 主页卡片查询业务服务。
 */
public interface UserHomeCardService {

    /**
     * 查询 HomeCard 展示资料，target 复用 UserProfileView。
     *
     * @param query self / target 用户 ID
     * @return HomeCard 视图
     * @throws UserBizException 参数非法、用户不可见或资料不存在时
     */
    HomeCardProfileVO getHomeCardProfile(GetHomeCardProfileQuery query);
}
