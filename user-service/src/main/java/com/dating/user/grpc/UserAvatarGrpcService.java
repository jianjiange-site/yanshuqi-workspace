package com.dating.user.grpc;

import com.dating.user.dto.ConfirmAvatarUploadCommand;
import com.dating.user.dto.PresignAvatarUploadCommand;
import com.dating.user.grpc.proto.AvatarView;
import com.dating.user.grpc.proto.ConfirmAvatarUploadRequest;
import com.dating.user.grpc.proto.ConfirmAvatarUploadResponse;
import com.dating.user.grpc.proto.PresignAvatarUploadRequest;
import com.dating.user.grpc.proto.PresignAvatarUploadResponse;
import com.dating.user.grpc.proto.UserAvatarServiceGrpc;
import com.dating.user.service.UserAvatarService;
import com.dating.user.vo.AvatarViewVO;
import com.dating.user.vo.PresignAvatarUploadResult;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.context.annotation.Profile;

/**
 * 用户头像上传 gRPC 服务，仅负责参数转换与结果映射。
 */
@GrpcService
@Profile("!test")
public class UserAvatarGrpcService extends UserAvatarServiceGrpc.UserAvatarServiceImplBase {

    private final UserAvatarService userAvatarService;

    public UserAvatarGrpcService(UserAvatarService userAvatarService) {
        this.userAvatarService = userAvatarService;
    }

    @Override
    public void presignAvatarUpload(PresignAvatarUploadRequest request,
                                    StreamObserver<PresignAvatarUploadResponse> responseObserver) {
        PresignAvatarUploadCommand command = new PresignAvatarUploadCommand();
        command.setUserId(request.getUserId());
        command.setExt(request.getExt());
        command.setExpectedSizeBytes(request.getExpectedSizeBytes());

        PresignAvatarUploadResult result = userAvatarService.presignAvatarUpload(command);

        PresignAvatarUploadResponse response = PresignAvatarUploadResponse.newBuilder()
                .setPresignedUrl(result.getPresignedUrl() == null ? "" : result.getPresignedUrl())
                .setObjectKey(result.getObjectKey() == null ? "" : result.getObjectKey())
                .setExpiresAtMs(result.getExpiresAtMs() == null ? 0L : result.getExpiresAtMs())
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void confirmAvatarUpload(ConfirmAvatarUploadRequest request,
                                    StreamObserver<ConfirmAvatarUploadResponse> responseObserver) {
        ConfirmAvatarUploadCommand command = new ConfirmAvatarUploadCommand();
        command.setUserId(request.getUserId());
        command.setObjectKey(request.getObjectKey());

        AvatarViewVO avatar = userAvatarService.confirmAvatarUpload(command);

        ConfirmAvatarUploadResponse response = ConfirmAvatarUploadResponse.newBuilder()
                .setAvatar(toAvatarView(avatar))
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    private AvatarView toAvatarView(AvatarViewVO vo) {
        if (vo == null) {
            return AvatarView.getDefaultInstance();
        }
        AvatarView.Builder builder = AvatarView.newBuilder()
                .setOriginalKey(vo.getOriginalKey() == null ? "" : vo.getOriginalKey())
                .setMinKey(vo.getMinKey() == null ? "" : vo.getMinKey())
                .setMidKey(vo.getMidKey() == null ? "" : vo.getMidKey())
                .setWidth(vo.getWidth())
                .setHeight(vo.getHeight());
        return builder.build();
    }
}
