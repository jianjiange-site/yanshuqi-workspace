package com.dating.gateway.controller;

import com.dating.gateway.dto.vo.CallbackResponse;
import com.dating.gateway.service.ImBffService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * OpenIM 服务端回调入口：非 App 用户请求，不要求 JWT；本阶段仅接收契约，不处理真实消息业务。
 */
@RestController
@RequestMapping("/callback/openim")
@Tag(name = "OpenIM Callback", description = "OpenIM 服务端回调（无 JWT）")
public class OpenImCallbackController {

    private final ImBffService imBffService;

    public OpenImCallbackController(ImBffService imBffService) {
        this.imBffService = imBffService;
    }

    @PostMapping("/{callbackCommand}")
    @Operation(summary = "OpenIM 回调")
    public CallbackResponse callback(@PathVariable("callbackCommand") String callbackCommand,
                                     @RequestHeader(value = "operationID", required = false) String operationId,
                                     @RequestBody(required = false) String rawBody) {
        return imBffService.handleOpenImCallback(callbackCommand, operationId, rawBody);
    }
}
