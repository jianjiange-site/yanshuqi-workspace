package com.dating.match.client.grpc;

import com.dating.match.exception.MatchBizException;
import com.dating.match.exception.MatchErrorCode;

/**
 * 外部 gRPC client 防腐层公共工具。
 */
public final class GrpcClientSupport {

    private GrpcClientSupport() {
    }

    public static MatchBizException notImplemented(String service, String rpc) {
        return new MatchBizException(MatchErrorCode.EXTERNAL_RPC_NOT_IMPLEMENTED,
                service + " " + rpc + " not implemented");
    }
}
