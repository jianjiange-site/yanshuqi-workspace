package com.dating.gateway.service;

import com.dating.gateway.dto.vo.HomeCardVO;

/**
 * Home BFF：主页卡片资料聚合（本阶段仅 user-service target 资料，不含 match/im 在线状态）。
 */
public interface HomeBffService {

    HomeCardVO getHomeCard(long callerUserId, long targetUserId);
}
