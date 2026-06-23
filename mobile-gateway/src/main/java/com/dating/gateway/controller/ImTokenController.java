package com.dating.gateway.controller;

import com.dating.gateway.common.Result;
import com.dating.gateway.dto.CallTokenReq;
import com.dating.gateway.dto.vo.CallTokenVO;
import com.dating.gateway.dto.vo.ImTokenVO;
import com.dating.gateway.resolver.CallerUserResolver;
import com.dating.gateway.service.ImBffService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * IM / Call token REST 入口：JWT 鉴权；真实 token 签发依赖 im-service，gateway 仅做契约转发。
 */
@RestController
@Tag(name = "IM", description = "OpenIM token、LiveKit call token")
public class ImTokenController {

    private final CallerUserResolver callerUserResolver;
    private final ImBffService imBffService;

    public ImTokenController(CallerUserResolver callerUserResolver, ImBffService imBffService) {
        this.callerUserResolver = callerUserResolver;
        this.imBffService = imBffService;
    }

    @GetMapping("/api/v1/im/token")
    @Operation(summary = "获取 OpenIM 用户 token")
    public Result<ImTokenVO> imToken(HttpServletRequest request) {
        long callerUserId = callerUserResolver.resolveCallerUserId(request);
        return Result.ok(imBffService.getImToken(callerUserId));
    }

    @PostMapping("/api/v1/call/token")
    @Operation(summary = "获取 LiveKit call token")
    public Result<CallTokenVO> callToken(HttpServletRequest request, @RequestBody CallTokenReq req) {
        long callerUserId = callerUserResolver.resolveCallerUserId(request);
        return Result.ok(imBffService.getCallToken(callerUserId, req));
    }
}
