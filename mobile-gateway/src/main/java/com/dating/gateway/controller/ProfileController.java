package com.dating.gateway.controller;

import com.dating.gateway.common.Result;
import com.dating.gateway.dto.UpdateProfileReq;
import com.dating.gateway.dto.UpsertOnboardingReq;
import com.dating.gateway.dto.vo.UserProfileVO;
import com.dating.gateway.resolver.CallerUserResolver;
import com.dating.gateway.service.ProfileBffService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Profile REST 入口：callerUserId 仅来自 JWT（dev/test 可 X-User-Id 兜底）。
 */
@RestController
@RequestMapping("/api/v1/profile")
@Tag(name = "Profile", description = "用户资料 onboarding 与日常更新")
public class ProfileController {

    private final CallerUserResolver callerUserResolver;
    private final ProfileBffService profileBffService;

    public ProfileController(CallerUserResolver callerUserResolver, ProfileBffService profileBffService) {
        this.callerUserResolver = callerUserResolver;
        this.profileBffService = profileBffService;
    }

    @PostMapping("/onboarding")
    @Operation(summary = "首次完善资料")
    public Result<UserProfileVO> upsertOnboarding(HttpServletRequest request,
                                                @RequestBody UpsertOnboardingReq req) {
        long callerUserId = callerUserResolver.resolveCallerUserId(request);
        UserProfileVO data = profileBffService.upsertOnboarding(callerUserId, req);
        return Result.ok(data);
    }

    @PatchMapping
    @Operation(summary = "日常修改资料（不含 gender / birthday）")
    public Result<Boolean> updateProfile(HttpServletRequest request, @RequestBody UpdateProfileReq req) {
        long callerUserId = callerUserResolver.resolveCallerUserId(request);
        boolean success = profileBffService.updateProfile(callerUserId, req);
        return Result.ok(success);
    }
}
