package com.dating.gateway.service.impl;

import com.dating.gateway.dto.CallTokenReq;
import com.dating.gateway.dto.vo.CallTokenVO;
import com.dating.gateway.dto.vo.CallbackResponse;
import com.dating.gateway.dto.vo.ImTokenVO;
import com.dating.gateway.exception.GatewayBizException;
import com.dating.gateway.exception.GatewayErrorCode;
import com.dating.gateway.service.ImBffService;
import com.dating.gateway.support.GatewayFeatureNotReadySupport;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

/**
 * IM BFF 生产实现：im-service 尚无正式 proto，prod/dev 统一返回 not ready。
 */
@Service
@Profile("!mock & !test")
public class ImBffServiceImpl implements ImBffService {

    @Override
    public ImTokenVO getImToken(long callerUserId) {
        throw GatewayFeatureNotReadySupport.imNotReady();
    }

    @Override
    public CallTokenVO getCallToken(long callerUserId, CallTokenReq req) {
        validateCallTokenReq(req);
        throw GatewayFeatureNotReadySupport.callNotReady();
    }

    @Override
    public CallbackResponse handleOpenImCallback(String callbackCommand, String operationId, String rawBody) {
        // OpenIM 回调占位：不解析业务，明确告知转发能力未接入
        CallbackResponse response = new CallbackResponse();
        response.setActionCode(1);
        response.setErrCode(GatewayErrorCode.CALLBACK_SERVICE_NOT_READY.getCode());
        response.setErrMsg(GatewayErrorCode.CALLBACK_SERVICE_NOT_READY.getMessage());
        response.setErrDlt("callbackCommand=" + callbackCommand);
        return response;
    }

    private void validateCallTokenReq(CallTokenReq req) {
        if (req == null || req.getPeerId() == null || req.getPeerId() <= 0) {
            throw new GatewayBizException(GatewayErrorCode.INVALID_ARGUMENT, "peerId 不能为空");
        }
    }
}
