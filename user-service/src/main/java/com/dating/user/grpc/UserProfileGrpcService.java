package com.dating.user.grpc;

import com.dating.user.dto.UpdateProfileCommand;
import com.dating.user.grpc.proto.GetSelfProfileRequest;
import com.dating.user.grpc.proto.GetSelfProfileResponse;
import com.dating.user.grpc.proto.UpdateProfileRequest;
import com.dating.user.grpc.proto.UpdateProfileResponse;
import com.dating.user.grpc.proto.UserProfileDetail;
import com.dating.user.grpc.proto.UserProfileServiceGrpc;
import com.dating.user.service.UserProfileService;
import com.dating.user.vo.UserProfileDetailVO;
import com.google.protobuf.Timestamp;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.context.annotation.Profile;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

/**
 * 用户资料 gRPC 服务，仅负责参数转换与结果映射。
 */
@GrpcService
@Profile("!test")
public class UserProfileGrpcService extends UserProfileServiceGrpc.UserProfileServiceImplBase {

    private final UserProfileService userProfileService;

    /**
     * 构造用户资料 gRPC 服务。
     *
     * @param userProfileService 用户资料业务服务
     */
    public UserProfileGrpcService(UserProfileService userProfileService) {
        this.userProfileService = userProfileService;
    }

    /**
     * 处理查询本人资料 gRPC 请求，仅做入参/出参转换。
     *
     * @param request          查询请求
     * @param responseObserver 响应观察者
     */
    @Override
    public void getSelfProfile(GetSelfProfileRequest request, StreamObserver<GetSelfProfileResponse> responseObserver) {
        // 1. 调用业务服务查询资料
        UserProfileDetailVO detail = userProfileService.getSelfProfile(request.getUserId());
        // 2. 转换为 gRPC Response
        GetSelfProfileResponse response = GetSelfProfileResponse.newBuilder()
                .setProfile(toUserProfileDetail(detail))
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    /**
     * 处理更新资料 gRPC 请求，仅做入参/出参转换。
     *
     * @param request          更新请求
     * @param responseObserver 响应观察者
     */
    @Override
    public void updateProfile(UpdateProfileRequest request, StreamObserver<UpdateProfileResponse> responseObserver) {
        // 1. 将 gRPC Request 转换为 Service 层 UpdateProfileCommand
        UpdateProfileCommand command = toUpdateProfileCommand(request);
        // 2. 调用业务服务更新资料
        UserProfileDetailVO detail = userProfileService.updateProfile(command);
        // 3. 转换为 gRPC Response
        UpdateProfileResponse response = UpdateProfileResponse.newBuilder()
                .setProfile(toUserProfileDetail(detail))
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    private UpdateProfileCommand toUpdateProfileCommand(UpdateProfileRequest request) {
        UpdateProfileCommand command = new UpdateProfileCommand();
        command.setUserId(request.getUserId());
        command.setNickname(emptyToNull(request.getNickname()));
        command.setGender(emptyToNull(request.getGender()));
        command.setBirthDate(parseBirthDate(request.getBirthDate()));
        command.setCountryCode(emptyToNull(request.getCountryCode()));
        command.setCityCode(emptyToNull(request.getCityCode()));
        command.setLanguageCodes(copyList(request.getLanguageCodesList()));
        command.setBio(emptyToNull(request.getBio()));
        command.setInterests(copyList(request.getInterestsList()));
        return command;
    }

    private UserProfileDetail toUserProfileDetail(UserProfileDetailVO detail) {
        UserProfileDetail.Builder builder = UserProfileDetail.newBuilder()
                .setUserId(detail.getUserId())
                .setUserType(defaultString(detail.getUserType()))
                .setAccountStatus(defaultString(detail.getAccountStatus()))
                .setProfileStatus(defaultString(detail.getProfileStatus()))
                .setNickname(defaultString(detail.getNickname()))
                .setGender(defaultString(detail.getGender()))
                .setBirthDate(detail.getBirthDate() == null ? "" : detail.getBirthDate().toString())
                .setCountryCode(defaultString(detail.getCountryCode()))
                .setCityCode(defaultString(detail.getCityCode()))
                .addAllLanguageCodes(defaultList(detail.getLanguageCodes()))
                .setBio(defaultString(detail.getBio()))
                .setAvatarKey(defaultString(detail.getAvatarKey()))
                .addAllInterests(defaultList(detail.getInterests()))
                .setProfileScore(detail.getProfileScore() == null ? 0 : detail.getProfileScore())
                .setProfileCompleted(detail.getProfileCompleted() == null ? 0 : detail.getProfileCompleted());
        if (detail.getCreatedAt() != null) {
            builder.setCreatedAt(toTimestamp(detail.getCreatedAt().toInstant()));
        }
        if (detail.getUpdatedAt() != null) {
            builder.setUpdatedAt(toTimestamp(detail.getUpdatedAt().toInstant()));
        }
        return builder.build();
    }

    private Timestamp toTimestamp(Instant instant) {
        return Timestamp.newBuilder()
                .setSeconds(instant.getEpochSecond())
                .setNanos(instant.getNano())
                .build();
    }

    private LocalDate parseBirthDate(String birthDate) {
        if (birthDate == null || birthDate.isBlank()) {
            return null;
        }
        return LocalDate.parse(birthDate.trim());
    }

    private String emptyToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }

    private List<String> copyList(List<String> values) {
        return values == null || values.isEmpty() ? null : List.copyOf(values);
    }

    private String defaultString(String value) {
        return value == null ? "" : value;
    }

    private List<String> defaultList(List<String> values) {
        return values == null ? List.of() : values;
    }
}
