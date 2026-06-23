package com.dating.gateway.client;

import com.dating.gateway.support.GatewayGrpcMetadataSupport;
import io.grpc.Metadata;

/**
 * post-service gRPC Metadata 构建；委托 {@link GatewayGrpcMetadataSupport} 保持通用能力一致。
 *
 * @deprecated 新代码请直接使用 {@link GatewayGrpcMetadataSupport}，本类保留兼容 Post 现有引用。
 */
@Deprecated
public final class PostGrpcMetadataSupport {

    public static final Metadata.Key<String> USER_ID_KEY = GatewayGrpcMetadataSupport.USER_ID_KEY;
    public static final Metadata.Key<String> TRACE_ID_KEY = GatewayGrpcMetadataSupport.TRACE_ID_KEY;
    public static final Metadata.Key<String> DEVICE_ID_KEY = GatewayGrpcMetadataSupport.DEVICE_ID_KEY;

    private PostGrpcMetadataSupport() {
    }

    public static Metadata buildMetadata(long callerUserId) {
        return GatewayGrpcMetadataSupport.buildMetadata(callerUserId);
    }
}
