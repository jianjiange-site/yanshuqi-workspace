package com.dating.user.grpc;

import com.dating.user.dto.BatchGetBasicProfilesQuery;
import com.dating.user.dto.BatchGetRecommendProfilesQuery;
import com.dating.user.dto.BindPhotoCommand;
import com.dating.user.dto.CheckUserAvailableQuery;
import com.dating.user.dto.GetHomeCardProfileQuery;
import com.dating.user.dto.ListUserPhotosQuery;
import com.dating.user.dto.UpdateProfileCommand;
import com.dating.user.dto.UpsertOnboardingCommand;
import com.dating.user.grpc.proto.BatchGetBasicProfilesRequest;
import com.dating.user.grpc.proto.BatchGetBasicProfilesResponse;
import com.dating.user.grpc.proto.BatchGetRecommendProfilesRequest;
import com.dating.user.grpc.proto.BatchGetRecommendProfilesResponse;
import com.dating.user.grpc.proto.BasicUserProfile;
import com.dating.user.grpc.proto.BindUserPhotoRequest;
import com.dating.user.grpc.proto.BindUserPhotoResponse;
import com.dating.user.grpc.proto.CheckUserAvailableRequest;
import com.dating.user.grpc.proto.CheckUserAvailableResponse;
import com.dating.user.grpc.proto.GetHomeCardProfileRequest;
import com.dating.user.grpc.proto.GetHomeCardProfileResponse;
import com.dating.user.grpc.proto.GetUserProfileViewRequest;
import com.dating.user.grpc.proto.GetUserProfileViewResponse;
import com.dating.user.grpc.proto.UpsertOnboardingRequest;
import com.dating.user.grpc.proto.UpsertOnboardingResponse;
import com.dating.user.grpc.proto.AvatarView;
import com.dating.user.grpc.proto.UserProfileView;
import com.dating.user.grpc.proto.GetSelfProfileRequest;
import com.dating.user.grpc.proto.GetSelfProfileResponse;
import com.dating.user.grpc.proto.ListUserPhotosRequest;
import com.dating.user.grpc.proto.ListUserPhotosResponse;
import com.dating.user.grpc.proto.RecommendUserProfile;
import com.dating.user.grpc.proto.UpdateProfileRequest;
import com.dating.user.grpc.proto.UpdateProfileResponse;
import com.dating.user.grpc.proto.UserAvailableResult;
import com.dating.user.grpc.proto.UserPhoto;
import com.dating.user.grpc.proto.UserProfileDetail;
import com.dating.user.grpc.proto.UserProfileServiceGrpc;
import com.dating.user.service.UserHomeCardService;
import com.dating.user.service.UserPhotoService;
import com.dating.user.service.UserProfileQueryService;
import com.dating.user.service.UserProfileService;
import com.dating.user.exception.UserBizException;
import com.dating.user.exception.UserErrorCode;
import com.dating.user.vo.BasicUserProfileVO;
import com.dating.user.vo.BindPhotoResult;
import com.dating.user.vo.HomeCardProfileVO;
import com.dating.user.vo.RecommendUserProfileVO;
import com.dating.user.vo.UserAvailableVO;
import com.dating.user.vo.UserPhotoVO;
import com.dating.user.vo.UserProfileDetailVO;
import com.dating.user.vo.UserProfileViewVO;
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
    private final UserProfileQueryService userProfileQueryService;
    private final UserHomeCardService userHomeCardService;

    /**
     * 构造用户资料 gRPC 服务。
     *
     * @param userProfileService      用户资料业务服务
     * @param userPhotoService        用户照片业务服务
     * @param userProfileQueryService 用户资料批量查询服务
     * @param userHomeCardService     主页卡片查询服务
     */
    public UserProfileGrpcService(UserProfileService userProfileService,
                                  UserPhotoService userPhotoService,
                                  UserProfileQueryService userProfileQueryService,
                                  UserHomeCardService userHomeCardService) {
        this.userProfileService = userProfileService;
        this.userPhotoService = userPhotoService;
        this.userProfileQueryService = userProfileQueryService;
        this.userHomeCardService = userHomeCardService;
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
                .setSuccess(true)
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void upsertOnboarding(UpsertOnboardingRequest request, StreamObserver<UpsertOnboardingResponse> responseObserver) {
        UserProfileViewVO view = userProfileService.upsertOnboarding(toUpsertOnboardingCommand(request));
        responseObserver.onNext(UpsertOnboardingResponse.newBuilder()
                .setProfile(toUserProfileView(view))
                .build());
        responseObserver.onCompleted();
    }

    @Override
    public void getUserProfileView(GetUserProfileViewRequest request,
                                   StreamObserver<GetUserProfileViewResponse> responseObserver) {
        UserProfileViewVO view = userProfileService.getUserProfileView(request.getUserId());
        responseObserver.onNext(GetUserProfileViewResponse.newBuilder()
                .setProfile(toUserProfileView(view))
                .build());
        responseObserver.onCompleted();
    }

    @Override
    public void getHomeCardProfile(GetHomeCardProfileRequest request,
                                   StreamObserver<GetHomeCardProfileResponse> responseObserver) {
        GetHomeCardProfileQuery query = new GetHomeCardProfileQuery();
        query.setSelfUserId(request.getSelfUserId());
        query.setTargetUserId(request.getTargetUserId());
        HomeCardProfileVO card = userHomeCardService.getHomeCardProfile(query);
        if (card.getTargetProfile() == null) {
            throw new UserBizException(UserErrorCode.HOME_CARD_QUERY_FAILED);
        }
        GetHomeCardProfileResponse response = GetHomeCardProfileResponse.newBuilder()
                .setSelfUserId(card.getSelfUserId() == null ? 0L : card.getSelfUserId())
                .setTargetProfile(toUserProfileView(card.getTargetProfile()))
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

    /**
     * 处理批量查询基础资料 gRPC 请求，仅做入参/出参转换。
     *
     * @param request          批量查询请求
     * @param responseObserver 响应观察者
     */
    @Override
    public void batchGetBasicProfiles(BatchGetBasicProfilesRequest request,
                                      StreamObserver<BatchGetBasicProfilesResponse> responseObserver) {
        BatchGetBasicProfilesQuery query = new BatchGetBasicProfilesQuery();
        query.setUserIds(request.getUserIdsList());
        query.setIncludeUnavailable(request.getIncludeUnavailable());
        List<BasicUserProfileVO> profiles = userProfileQueryService.batchGetBasicProfiles(query);
        BatchGetBasicProfilesResponse.Builder builder = BatchGetBasicProfilesResponse.newBuilder();
        for (BasicUserProfileVO profile : profiles) {
            builder.addProfiles(toBasicUserProfile(profile));
        }
        responseObserver.onNext(builder.build());
        responseObserver.onCompleted();
    }

    /**
     * 处理批量查询推荐展示资料 gRPC 请求，仅做入参/出参转换。
     *
     * @param request          批量查询请求
     * @param responseObserver 响应观察者
     */
    @Override
    public void batchGetRecommendProfiles(BatchGetRecommendProfilesRequest request,
                                          StreamObserver<BatchGetRecommendProfilesResponse> responseObserver) {
        BatchGetRecommendProfilesQuery query = new BatchGetRecommendProfilesQuery();
        query.setUserIds(request.getUserIdsList());
        query.setIncludeUnavailable(request.getIncludeUnavailable());
        List<RecommendUserProfileVO> profiles = userProfileQueryService.batchGetRecommendProfiles(query);
        BatchGetRecommendProfilesResponse.Builder builder = BatchGetRecommendProfilesResponse.newBuilder();
        for (RecommendUserProfileVO profile : profiles) {
            builder.addProfiles(toRecommendUserProfile(profile));
        }
        responseObserver.onNext(builder.build());
        responseObserver.onCompleted();
    }

    /**
     * 处理批量检查用户可用性 gRPC 请求，仅做入参/出参转换。
     *
     * @param request          检查请求
     * @param responseObserver 响应观察者
     */
    @Override
    public void checkUserAvailable(CheckUserAvailableRequest request,
                                   StreamObserver<CheckUserAvailableResponse> responseObserver) {
        CheckUserAvailableQuery query = new CheckUserAvailableQuery();
        query.setUserIds(request.getUserIdsList());
        List<UserAvailableVO> results = userProfileQueryService.checkUserAvailable(query);
        CheckUserAvailableResponse.Builder builder = CheckUserAvailableResponse.newBuilder();
        for (UserAvailableVO result : results) {
            builder.addResults(toUserAvailableResult(result));
        }
        responseObserver.onNext(builder.build());
        responseObserver.onCompleted();
    }

    private BasicUserProfile toBasicUserProfile(BasicUserProfileVO vo) {
        return BasicUserProfile.newBuilder()
                .setUserId(vo.getUserId() == null ? 0L : vo.getUserId())
                .setNickname(defaultString(vo.getNickname()))
                .setGender(defaultString(vo.getGender()))
                .setCityCode(defaultString(vo.getCityCode()))
                .setAvatarKey(defaultString(vo.getAvatarKey()))
                .setProfileStatus(defaultString(vo.getProfileStatus()))
                .setAccountStatus(defaultString(vo.getAccountStatus()))
                .setAvailable(vo.isAvailable())
                .setUnavailableReason(defaultString(vo.getUnavailableReason()))
                .build();
    }

    private RecommendUserProfile toRecommendUserProfile(RecommendUserProfileVO vo) {
        return RecommendUserProfile.newBuilder()
                .setUserId(vo.getUserId() == null ? 0L : vo.getUserId())
                .setUserType(defaultString(vo.getUserType()))
                .setGender(defaultString(vo.getGender()))
                .setBirthDate(vo.getBirthDate() == null ? "" : vo.getBirthDate().toString())
                .setCountryCode(defaultString(vo.getCountryCode()))
                .setCityCode(defaultString(vo.getCityCode()))
                .addAllLanguageCodes(defaultList(vo.getLanguageCodes()))
                .addAllInterests(defaultList(vo.getInterests()))
                .setBio(defaultString(vo.getBio()))
                .setAvatarKey(defaultString(vo.getAvatarKey()))
                .setProfileScore(vo.getProfileScore() == null ? 0 : vo.getProfileScore())
                .setProfileCompleted(vo.getProfileCompleted() == null ? 0 : vo.getProfileCompleted())
                .setProfileStatus(defaultString(vo.getProfileStatus()))
                .setAccountStatus(defaultString(vo.getAccountStatus()))
                .setAvailable(vo.isAvailable())
                .setUnavailableReason(defaultString(vo.getUnavailableReason()))
                .build();
    }

    private UserAvailableResult toUserAvailableResult(UserAvailableVO vo) {
        return UserAvailableResult.newBuilder()
                .setUserId(vo.getUserId() == null ? 0L : vo.getUserId())
                .setAvailable(vo.isAvailable())
                .setAccountStatus(defaultString(vo.getAccountStatus()))
                .setProfileStatus(defaultString(vo.getProfileStatus()))
                .setReason(defaultString(vo.getReason()))
                .build();
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

    private UpsertOnboardingCommand toUpsertOnboardingCommand(UpsertOnboardingRequest request) {
        UpsertOnboardingCommand command = new UpsertOnboardingCommand();
        command.setUserId(request.getUserId());
        command.setNickname(emptyToNull(request.getNickname()));
        command.setGender(emptyToNull(request.getGender()));
        command.setBirthday(emptyToNull(request.getBirthday()));
        if (request.hasAge()) {
            command.setAge(request.getAge());
        }
        if (request.hasHeight()) {
            command.setHeight(request.getHeight());
        }
        command.setBio(emptyToNull(request.getBio()));
        command.setOccupation(emptyToNull(request.getOccupation()));
        command.setEducation(emptyToNull(request.getEducation()));
        command.setLocation(emptyToNull(request.getLocation()));
        command.setDefaultAvatarObjectKey(emptyToNull(request.getDefaultAvatarObjectKey()));
        return command;
    }

    private UserProfileView toUserProfileView(UserProfileViewVO view) {
        UserProfileView.Builder builder = UserProfileView.newBuilder()
                .setUserId(view.getUserId() == null ? 0L : view.getUserId())
                .setNickname(defaultString(view.getNickname()))
                .setAge(view.getAge() == null ? 0 : view.getAge())
                .setGender(defaultString(view.getGender()))
                .setHeight(view.getHeight() == null ? 0 : view.getHeight())
                .setBio(defaultString(view.getBio()))
                .setOccupation(defaultString(view.getOccupation()))
                .setEducation(defaultString(view.getEducation()))
                .setLocation(defaultString(view.getLocation()))
                .setBirthday(defaultString(view.getBirthday()))
                .addAllInterests(defaultList(view.getInterests()))
                .setPending(view.isPending())
                .setRegulationStatus(view.getRegulationStatus() == null ? 0 : view.getRegulationStatus())
                .setLastOpenAtMs(view.getLastOpenAtMs() == null ? 0L : view.getLastOpenAtMs());
        if (view.getAvatar() != null) {
            builder.setAvatar(AvatarView.newBuilder()
                    .setOriginalKey(defaultString(view.getAvatar().getOriginalKey()))
                    .setMinKey(defaultString(view.getAvatar().getMinKey()))
                    .setMidKey(defaultString(view.getAvatar().getMidKey()))
                    .setWidth(view.getAvatar().getWidth())
                    .setHeight(view.getAvatar().getHeight())
                    .build());
        }
        return builder.build();
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
        if (request.hasAge()) {
            command.setAgePresent(true);
            command.setAge(request.getAge());
        }
        if (request.hasHeight()) {
            command.setHeightPresent(true);
            command.setHeight(request.getHeight());
        }
        command.setOccupation(emptyToNull(request.getOccupation()));
        command.setEducation(emptyToNull(request.getEducation()));
        command.setLocation(emptyToNull(request.getLocation()));
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
