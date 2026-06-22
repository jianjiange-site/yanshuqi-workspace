package com.dating.match.client.grpc;

import com.dating.match.client.TargetUserTypeResolver;
import com.dating.match.exception.MatchBizException;
import com.dating.match.exception.MatchErrorCode;
import com.dating.user.grpc.proto.BatchGetRecommendProfilesRequest;
import com.dating.user.grpc.proto.BatchGetRecommendProfilesResponse;
import com.dating.user.grpc.proto.RecommendUserProfile;
import com.dating.user.grpc.proto.UserProfileServiceGrpc;
import io.grpc.StatusRuntimeException;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * user-service gRPC 目标用户类型解析。
 */
@Component
@ConditionalOnProperty(prefix = "app.match.external", name = "user-client-mode", havingValue = "grpc")
public class UserServiceTargetUserTypeResolver implements TargetUserTypeResolver {

    private static final Logger log = LoggerFactory.getLogger(UserServiceTargetUserTypeResolver.class);

    private final UserProfileServiceGrpc.UserProfileServiceBlockingStub userProfileStub;

    public UserServiceTargetUserTypeResolver(
            @GrpcClient("user-service") UserProfileServiceGrpc.UserProfileServiceBlockingStub userProfileStub) {
        this.userProfileStub = userProfileStub;
    }

    @Override
    public int resolveTargetUserType(long userId) {
        try {
            BatchGetRecommendProfilesResponse response = userProfileStub.batchGetRecommendProfiles(
                    BatchGetRecommendProfilesRequest.newBuilder()
                            .addUserIds(userId)
                            .setIncludeUnavailable(true)
                            .build());
            if (response.getProfilesCount() == 0) {
                throw new MatchBizException(MatchErrorCode.TARGET_NOT_FOUND);
            }
            RecommendUserProfile profile = response.getProfiles(0);
            return UserProfileProtoMapper.parseUserType(profile.getUserType());
        } catch (StatusRuntimeException ex) {
            log.error("user-service resolveTargetUserType failed userId={}", userId, ex);
            throw new MatchBizException(MatchErrorCode.INTERNAL_ERROR,
                    "user-service BatchGetRecommendProfiles failed: " + ex.getStatus());
        }
    }
}
