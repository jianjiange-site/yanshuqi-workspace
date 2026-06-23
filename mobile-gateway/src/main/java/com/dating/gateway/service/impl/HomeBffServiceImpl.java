package com.dating.gateway.service.impl;

import com.dating.gateway.client.UserProfileGrpcClient;
import com.dating.gateway.converter.UserProfileProtoAdapter;
import com.dating.gateway.converter.UserProfileReqBuilder;
import com.dating.gateway.dto.vo.HomeCardVO;
import com.dating.gateway.exception.GatewayBizException;
import com.dating.gateway.exception.GatewayErrorCode;
import com.dating.gateway.service.HomeBffService;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

/**
 * Home BFF 实现：selfUserId = callerUserId，target 资料来自 user-service。
 * <p>
 * 后续可在此扩展 match 关系、visit 统计、IM 在线状态等聚合，本阶段不实现。
 */
@Service
@Profile("!test")
public class HomeBffServiceImpl implements HomeBffService {

    private final UserProfileGrpcClient userProfileGrpcClient;

    public HomeBffServiceImpl(UserProfileGrpcClient userProfileGrpcClient) {
        this.userProfileGrpcClient = userProfileGrpcClient;
    }

    @Override
    public HomeCardVO getHomeCard(long callerUserId, long targetUserId) {
        validateTargetUserId(targetUserId);
        var response = userProfileGrpcClient.getHomeCardProfile(
                UserProfileReqBuilder.buildGetHomeCard(callerUserId, targetUserId));
        return UserProfileProtoAdapter.toHomeCardVO(response);
    }

    private void validateTargetUserId(long targetUserId) {
        if (targetUserId <= 0) {
            throw new GatewayBizException(GatewayErrorCode.INVALID_ARGUMENT, "targetId 非法");
        }
    }
}
