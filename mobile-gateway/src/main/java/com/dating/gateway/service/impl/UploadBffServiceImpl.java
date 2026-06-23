package com.dating.gateway.service.impl;

import com.dating.gateway.client.UserProfileGrpcClient;
import com.dating.gateway.converter.UserProfileProtoAdapter;
import com.dating.gateway.converter.UserProfileReqBuilder;
import com.dating.gateway.dto.ConfirmAvatarReq;
import com.dating.gateway.dto.PresignAvatarReq;
import com.dating.gateway.dto.vo.AvatarVO;
import com.dating.gateway.dto.vo.PresignAvatarUploadVO;
import com.dating.gateway.exception.GatewayBizException;
import com.dating.gateway.exception.GatewayErrorCode;
import com.dating.gateway.service.UploadBffService;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.Set;

/**
 * Upload BFF 实现：网关侧校验 ext / 大小后代理 user-service presign/confirm。
 */
@Service
@Profile("!test")
public class UploadBffServiceImpl implements UploadBffService {

    private static final Set<String> ALLOWED_EXT = Set.of("jpg", "jpeg", "png", "webp");
    private static final long MAX_SIZE_BYTES = 10L * 1024 * 1024;

    private final UserProfileGrpcClient userProfileGrpcClient;

    public UploadBffServiceImpl(UserProfileGrpcClient userProfileGrpcClient) {
        this.userProfileGrpcClient = userProfileGrpcClient;
    }

    @Override
    public PresignAvatarUploadVO presignAvatar(long callerUserId, PresignAvatarReq req) {
        validatePresignRequest(req);
        String ext = UserProfileReqBuilder.normalizeExt(req.getExt());
        var response = userProfileGrpcClient.presignAvatarUpload(
                UserProfileReqBuilder.buildPresignAvatar(callerUserId, ext, req.getExpectedSizeBytes()));
        return UserProfileProtoAdapter.toPresignAvatarUploadVO(response);
    }

    @Override
    public AvatarVO confirmAvatar(long callerUserId, ConfirmAvatarReq req) {
        var response = userProfileGrpcClient.confirmAvatarUpload(
                UserProfileReqBuilder.buildConfirmAvatar(callerUserId, req));
        return UserProfileProtoAdapter.toAvatarVO(response);
    }

    /**
     * 网关侧 presign 参数校验，减轻无效 gRPC 调用。
     */
    private void validatePresignRequest(PresignAvatarReq req) {
        if (req.getExpectedSizeBytes() == null || req.getExpectedSizeBytes() <= 0) {
            throw new GatewayBizException(GatewayErrorCode.INVALID_ARGUMENT, "expectedSizeBytes 必填且须大于 0");
        }
        if (req.getExpectedSizeBytes() > MAX_SIZE_BYTES) {
            throw new GatewayBizException(GatewayErrorCode.INVALID_ARGUMENT, "文件大小不能超过 10MB");
        }
        String ext = UserProfileReqBuilder.normalizeExt(req.getExt());
        if (!ALLOWED_EXT.contains(ext)) {
            throw new GatewayBizException(GatewayErrorCode.INVALID_ARGUMENT, "ext 仅支持 jpg/jpeg/png/webp");
        }
    }
}
