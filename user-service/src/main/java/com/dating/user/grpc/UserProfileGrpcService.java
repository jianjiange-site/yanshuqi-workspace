package com.dating.user.grpc;

import com.dating.user.dto.BindPhotoCommand;
import com.dating.user.dto.ListUserPhotosQuery;
import com.dating.user.dto.UpdateProfileCommand;
import com.dating.user.grpc.proto.BindUserPhotoRequest;
import com.dating.user.grpc.proto.BindUserPhotoResponse;
import com.dating.user.grpc.proto.GetSelfProfileRequest;
import com.dating.user.grpc.proto.GetSelfProfileResponse;
import com.dating.user.grpc.proto.ListUserPhotosRequest;
import com.dating.user.grpc.proto.ListUserPhotosResponse;
import com.dating.user.grpc.proto.UpdateProfileRequest;
import com.dating.user.grpc.proto.UpdateProfileResponse;
import com.dating.user.grpc.proto.UserPhoto;
import com.dating.user.grpc.proto.UserProfileDetail;
import com.dating.user.grpc.proto.UserProfileServiceGrpc;
import com.dating.user.service.UserPhotoService;
import com.dating.user.service.UserProfileService;
import com.dating.user.vo.BindPhotoResult;
import com.dating.user.vo.UserPhotoVO;
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
    private final UserPhotoService userPhotoService;

    /**
     * 构造用户资料 gRPC 服务。
     *
     * @param userProfileService 用户资料业务服务
     * @param userPhotoService   用户照片业务服务
     */
    public UserProfileGrpcService(UserProfileService userProfileService, UserPhotoService userPhotoService) {
        this.userProfileService = userProfileService;
        this.userPhotoService = userPhotoService;
    }

    /**
     * 处理查询本人资料 gRPC 请求，仅做入参/出参转换。
     *
     * @param request          查询请求
     * @param responseObserver 响应观察者
     */
    @Override
    public void getSelfProfile(GetSelfProfileRequest request, StreamObserver<GetSelfProfileResponse> responseObserver) {
        UserProfileDetailVO detail = userProfileService.getSelfProfile(request.getUserId());
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
        UpdateProfileCommand command = toUpdateProfileCommand(request);
        UserProfileDetailVO detail = userProfileService.updateProfile(command);
        UpdateProfileResponse response = UpdateProfileResponse.newBuilder()
                .setProfile(toUserProfileDetail(detail))
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    /**
     * 处理绑定照片 gRPC 请求，仅做入参/出参转换。
     *
     * @param request          绑定请求
     * @param responseObserver 响应观察者
     */
    @Override
    public void bindUserPhoto(BindUserPhotoRequest request, StreamObserver<BindUserPhotoResponse> responseObserver) {
        BindPhotoCommand command = toBindPhotoCommand(request);
        BindPhotoResult result = userPhotoService.bindUserPhoto(command);
        BindUserPhotoResponse response = BindUserPhotoResponse.newBuilder()
                .setPhoto(toUserPhoto(result.getPhoto()))
                .setAvatarKey(defaultString(result.getAvatarKey()))
                .setProfileStatus(defaultString(result.getProfileStatus()))
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    /**
     * 处理查询照片列表 gRPC 请求，仅做入参/出参转换。
     *
     * @param request          查询请求
     * @param responseObserver 响应观察者
     */
    @Override
    public void listUserPhotos(ListUserPhotosRequest request, StreamObserver<ListUserPhotosResponse> responseObserver) {
        ListUserPhotosQuery query = toListUserPhotosQuery(request);
        List<UserPhotoVO> photos = userPhotoService.listUserPhotos(query);
        ListUserPhotosResponse.Builder builder = ListUserPhotosResponse.newBuilder();
        for (UserPhotoVO photo : photos) {
            builder.addPhotos(toUserPhoto(photo));
        }
        responseObserver.onNext(builder.build());
        responseObserver.onCompleted();
    }

    private BindPhotoCommand toBindPhotoCommand(BindUserPhotoRequest request) {
        BindPhotoCommand command = new BindPhotoCommand();
        command.setUserId(request.getUserId());
        command.setPhotoType(emptyToNull(request.getPhotoType()));
        command.setObjectKey(emptyToNull(request.getObjectKey()));
        command.setSortOrder(request.getSortOrder());
        return command;
    }

    private ListUserPhotosQuery toListUserPhotosQuery(ListUserPhotosRequest request) {
        ListUserPhotosQuery query = new ListUserPhotosQuery();
        query.setUserId(request.getUserId());
        query.setPhotoType(emptyToNull(request.getPhotoType()));
        query.setIncludeDisabled(false);
        return query;
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

    private UserPhoto toUserPhoto(UserPhotoVO vo) {
        UserPhoto.Builder builder = UserPhoto.newBuilder()
                .setPhotoId(vo.getPhotoId() == null ? 0L : vo.getPhotoId())
                .setUserId(vo.getUserId() == null ? 0L : vo.getUserId())
                .setPhotoType(defaultString(vo.getPhotoType()))
                .setObjectKey(defaultString(vo.getObjectKey()))
                .setSortOrder(vo.getSortOrder() == null ? 0 : vo.getSortOrder())
                .setReviewStatus(defaultString(vo.getReviewStatus()))
                .setEnabled(vo.getEnabled() == null ? 0 : vo.getEnabled());
        if (vo.getCreatedAt() != null) {
            builder.setCreatedAt(toTimestamp(vo.getCreatedAt().toInstant()));
        }
        if (vo.getUpdatedAt() != null) {
            builder.setUpdatedAt(toTimestamp(vo.getUpdatedAt().toInstant()));
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
