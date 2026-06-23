package com.dating.gateway.controller;

import com.dating.gateway.common.Result;
import com.dating.gateway.dto.vo.HomeCardVO;
import com.dating.gateway.exception.GatewayBizException;
import com.dating.gateway.exception.GatewayErrorCode;
import com.dating.gateway.resolver.CallerUserResolver;
import com.dating.gateway.service.HomeBffService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Home REST 入口：主页卡片资料；relation/im 在线状态留待后续聚合。
 */
@RestController
@RequestMapping("/api/v1/home")
@Tag(name = "Home", description = "主页卡片")
public class HomeController {

    private final CallerUserResolver callerUserResolver;
    private final HomeBffService homeBffService;

    public HomeController(CallerUserResolver callerUserResolver, HomeBffService homeBffService) {
        this.callerUserResolver = callerUserResolver;
        this.homeBffService = homeBffService;
    }

    @GetMapping("/card")
    @Operation(summary = "查询目标用户主页卡片")
    public Result<HomeCardVO> card(HttpServletRequest request,
                                   @RequestParam(value = "targetId", required = false) Long targetId) {
        long callerUserId = callerUserResolver.resolveCallerUserId(request);
        if (targetId == null) {
            throw new GatewayBizException(GatewayErrorCode.INVALID_ARGUMENT, "targetId 不能为空");
        }
        HomeCardVO data = homeBffService.getHomeCard(callerUserId, targetId);
        return Result.ok(data);
    }
}
