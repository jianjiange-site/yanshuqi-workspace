package com.dating.gateway.controller;

import com.dating.gateway.common.Result;
import com.dating.gateway.dto.ConfirmAvatarReq;
import com.dating.gateway.dto.PresignAvatarReq;
import com.dating.gateway.dto.vo.AvatarVO;
import com.dating.gateway.dto.vo.PresignAvatarUploadVO;
import com.dating.gateway.resolver.CallerUserResolver;
import com.dating.gateway.service.UploadBffService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Upload REST 入口：仅 presign/confirm 代理，不读取文件流、不持有对象存储凭证。
 */
@RestController
@RequestMapping("/api/v1/upload")
@Tag(name = "Upload", description = "头像上传 presign / confirm")
public class UploadController {

    private final CallerUserResolver callerUserResolver;
    private final UploadBffService uploadBffService;

    public UploadController(CallerUserResolver callerUserResolver, UploadBffService uploadBffService) {
        this.callerUserResolver = callerUserResolver;
        this.uploadBffService = uploadBffService;
    }

    @PostMapping("/presign")
    @Operation(summary = "签发头像 PUT presigned URL")
    public Result<PresignAvatarUploadVO> presign(HttpServletRequest request,
                                                 @Valid @RequestBody PresignAvatarReq req) {
        long callerUserId = callerUserResolver.resolveCallerUserId(request);
        PresignAvatarUploadVO data = uploadBffService.presignAvatar(callerUserId, req);
        return Result.ok(data);
    }

    @PostMapping("/confirm")
    @Operation(summary = "确认头像已上传")
    public Result<AvatarVO> confirm(HttpServletRequest request, @Valid @RequestBody ConfirmAvatarReq req) {
        long callerUserId = callerUserResolver.resolveCallerUserId(request);
        AvatarVO data = uploadBffService.confirmAvatar(callerUserId, req);
        return Result.ok(data);
    }
}
