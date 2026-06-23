package com.dating.gateway.service.impl;

import com.dating.gateway.dto.CallTokenReq;
import com.dating.gateway.dto.vo.CallTokenVO;
import com.dating.gateway.dto.vo.CallbackResponse;
import com.dating.gateway.dto.vo.ImTokenVO;
import com.dating.gateway.exception.GatewayBizException;
import com.dating.gateway.exception.GatewayErrorCode;
import com.dating.gateway.service.ImBffService;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

/**
 * IM BFF mock 实现：仅 mock/test profile 启用，禁止用于 prod。
 */
@Service
@Profile({"mock", "test"})
public class MockImBffServiceImpl implements ImBffService {

    @Override
    public ImTokenVO getImToken(long callerUserId) {
        // 真实 token 应由 im-service 调 OpenIM 签发，gateway 不持有 OpenIM secret
        ImTokenVO vo = new ImTokenVO();
        vo.setUserId(String.valueOf(callerUserId));
        vo.setImToken("mock-im-token-" + callerUserId);
        return vo;
    }

    @Override
    public CallTokenVO getCallToken(long callerUserId, CallTokenReq req) {
        validateCallTokenReq(req);
        // 真实 LiveKit token 应由 im-service 生成，gateway 不持有 LiveKit secret
        CallTokenVO vo = new CallTokenVO();
        vo.setRoomName("mock-room-" + callerUserId + "-" + req.getPeerId());
        vo.setToken("mock-livekit-token-" + callerUserId + "-" + req.getPeerId());
        return vo;
    }

    @Override
    public CallbackResponse handleOpenImCallback(String callbackCommand, String operationId, String rawBody) {
        CallbackResponse response = new CallbackResponse();
        response.setActionCode(0);
        response.setErrCode(0);
        response.setErrMsg("mock callback accepted");
        response.setErrDlt("callbackCommand=" + callbackCommand);
        return response;
    }

    private void validateCallTokenReq(CallTokenReq req) {
        if (req == null || req.getPeerId() == null || req.getPeerId() <= 0) {
            throw new GatewayBizException(GatewayErrorCode.INVALID_ARGUMENT, "peerId 不能为空");
        }
    }
}
