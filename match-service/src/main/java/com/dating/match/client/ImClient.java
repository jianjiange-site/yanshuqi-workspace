package com.dating.match.client;

/**
 * IM 副作用客户端抽象，后续替换为 im-service gRPC。
 */
public interface ImClient {

    /**
     * 执行 outbox 动作。
     *
     * @return 是否成功
     */
    boolean execute(String action, String payloadJson);
}
