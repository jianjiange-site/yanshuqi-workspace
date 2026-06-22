package com.dating.match.client.grpc;

import com.dating.match.client.CandidateClient;
import com.dating.match.exception.MatchBizException;
import com.dating.match.recommend.CandidateProfile;
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

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * user-service gRPC 候选/资料客户端：batchGetProfiles 走 BatchGetRecommendProfiles；
 * 候选召回 RPC 尚未在 user-service 提供，list* 方法明确失败。
 */
@Component
@ConditionalOnProperty(prefix = "app.match.external", name = "user-client-mode", havingValue = "grpc")
public class UserServiceCandidateClient implements CandidateClient {

    private static final Logger log = LoggerFactory.getLogger(UserServiceCandidateClient.class);

    private final UserProfileServiceGrpc.UserProfileServiceBlockingStub userProfileStub;

    public UserServiceCandidateClient(
            @GrpcClient("user-service") UserProfileServiceGrpc.UserProfileServiceBlockingStub userProfileStub) {
        this.userProfileStub = userProfileStub;
    }

    @Override
    public List<CandidateProfile> listDhCandidates(long callerUserId, int limit) {
        throw GrpcClientSupport.notImplemented("user-service", "listDhCandidates");
    }

    @Override
    public List<CandidateProfile> listBhCandidates(long callerUserId, int limit) {
        throw GrpcClientSupport.notImplemented("user-service", "listBhCandidates/nearbyUsers");
    }

    @Override
    public Map<Long, CandidateProfile> batchGetProfiles(Collection<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Map.of();
        }
        try {
            BatchGetRecommendProfilesResponse response = userProfileStub.batchGetRecommendProfiles(
                    BatchGetRecommendProfilesRequest.newBuilder()
                            .addAllUserIds(userIds)
                            .setIncludeUnavailable(false)
                            .build());
            Map<Long, CandidateProfile> result = new HashMap<>();
            for (RecommendUserProfile profile : response.getProfilesList()) {
                if (!profile.getAvailable()) {
                    continue;
                }
                result.put(profile.getUserId(), UserProfileProtoMapper.fromRecommendProfile(profile));
            }
            return result;
        } catch (StatusRuntimeException ex) {
            log.error("user-service BatchGetRecommendProfiles failed", ex);
            throw new MatchBizException(com.dating.match.exception.MatchErrorCode.INTERNAL_ERROR,
                    "user-service BatchGetRecommendProfiles failed: " + ex.getStatus());
        }
    }
}
