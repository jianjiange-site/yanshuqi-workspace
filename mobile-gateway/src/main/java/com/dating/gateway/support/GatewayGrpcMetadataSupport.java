package com.dating.gateway.support;

import com.dating.gateway.security.CurrentUserContext;
import com.dating.gateway.security.JwtClaims;
import io.grpc.Metadata;
import org.slf4j.MDC;
import org.springframework.util.StringUtils;

/**
 * 下游 gRPC 调用通用 Metadata 构建：x-user-id / x-trace-id / x-device-id。
 * <p>
 * 每次调用独立创建 Metadata，避免 ThreadLocal 并发污染；Post 等 client 复用此工具。
 */
public final class GatewayGrpcMetadataSupport {

    public static final Metadata.Key<String> USER_ID_KEY =
            Metadata.Key.of("x-user-id", Metadata.ASCII_STRING_MARSHALLER);

    public static final Metadata.Key<String> TRACE_ID_KEY =
            Metadata.Key.of("x-trace-id", Metadata.ASCII_STRING_MARSHALLER);

    public static final Metadata.Key<String> DEVICE_ID_KEY =
            Metadata.Key.of("x-device-id", Metadata.ASCII_STRING_MARSHALLER);

    private GatewayGrpcMetadataSupport() {
    }

    /**
     * @param callerUserId JWT 解析出的调用方 userId，禁止来自前端 Header
     */
    public static Metadata buildMetadata(long callerUserId) {
        Metadata metadata = new Metadata();
        metadata.put(USER_ID_KEY, String.valueOf(callerUserId));

        String traceId = MDC.get("traceId");
        if (StringUtils.hasText(traceId)) {
            metadata.put(TRACE_ID_KEY, traceId);
        }

        JwtClaims claims = CurrentUserContext.get();
        if (claims != null && StringUtils.hasText(claims.getDeviceId())) {
            metadata.put(DEVICE_ID_KEY, claims.getDeviceId());
        }
        return metadata;
    }
}
