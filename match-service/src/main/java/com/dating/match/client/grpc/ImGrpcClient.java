package com.dating.match.client.grpc;

import com.dating.match.client.ImClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * im-service gRPC 客户端占位；EnsureConversation 等 RPC 尚未定义。
 * <p>
 * 返回 false 触发 outbox 重试，不静默成功。
 */
@Component
@ConditionalOnProperty(prefix = "app.match.external", name = "im-client-mode", havingValue = "grpc")
public class ImGrpcClient implements ImClient {

    private static final Logger log = LoggerFactory.getLogger(ImGrpcClient.class);

    @Override
    public boolean execute(String action, String payloadJson) {
        log.error("im-service RPC not implemented action={} payload={}", action, payloadJson);
        return false;
    }
}
