package com.dating.post.grpc;

import io.grpc.ForwardingServerCallListener;
import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;
import net.devh.boot.grpc.server.interceptor.GrpcGlobalServerInterceptor;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

/**
 * 从 gRPC Metadata 读取 gateway 透传的 {@code x-user-id}，写入 {@link GrpcUserContext}。
 * <p>
 * 调用方身份不放在业务 request 字段中，避免客户端伪造 userId。
 */
@GrpcGlobalServerInterceptor
@Order(Ordered.HIGHEST_PRECEDENCE)
public class GrpcUserContextInterceptor implements ServerInterceptor {

    public static final Metadata.Key<String> USER_ID_METADATA_KEY =
            Metadata.Key.of("x-user-id", Metadata.ASCII_STRING_MARSHALLER);

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
            ServerCall<ReqT, RespT> call,
            Metadata headers,
            ServerCallHandler<ReqT, RespT> next) {
        String userIdHeader = headers.get(USER_ID_METADATA_KEY);
        if (userIdHeader != null && !userIdHeader.isBlank()) {
            try {
                GrpcUserContext.setUserId(Long.parseLong(userIdHeader.trim()));
            } catch (NumberFormatException ignored) {
                // 非法 header 不写入上下文，由业务方法 requireUserId 统一兜底。
            }
        }

        ServerCall.Listener<ReqT> delegate = next.startCall(call, headers);
        return new ForwardingServerCallListener.SimpleForwardingServerCallListener<>(delegate) {
            @Override
            public void onComplete() {
                try {
                    super.onComplete();
                } finally {
                    GrpcUserContext.clear();
                }
            }

            @Override
            public void onCancel() {
                try {
                    super.onCancel();
                } finally {
                    GrpcUserContext.clear();
                }
            }
        };
    }
}
