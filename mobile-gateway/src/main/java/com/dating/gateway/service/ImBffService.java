package com.dating.gateway.service;

import com.dating.gateway.dto.CallTokenReq;
import com.dating.gateway.dto.vo.CallTokenVO;
import com.dating.gateway.dto.vo.CallbackResponse;
import com.dating.gateway.dto.vo.ImTokenVO;

/**
 * IM / Call BFF：token 与 OpenIM 回调契约，真实逻辑待 im-service proto 就绪后接入。
 */
public interface ImBffService {

    /**
     * 获取 OpenIM 用户 token；真实 token 应由 im-service 调 OpenIM 签发，gateway 不持有 OpenIM secret。
     */
    ImTokenVO getImToken(long callerUserId);

    /**
     * 获取 LiveKit call token；真实 token 应由 im-service 生成，gateway 不持有 LiveKit secret。
     */
    CallTokenVO getCallToken(long callerUserId, CallTokenReq req);

    /**
     * OpenIM 服务端回调占位；后续应转发 im-service callback gRPC。
     */
    CallbackResponse handleOpenImCallback(String callbackCommand, String operationId, String rawBody);
}
