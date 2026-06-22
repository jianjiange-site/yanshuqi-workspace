package com.dating.match.client.grpc;

import com.dating.match.exception.MatchBizException;
import com.dating.match.exception.MatchErrorCode;
import com.dating.user.grpc.proto.BatchGetRecommendProfilesResponse;
import com.dating.user.grpc.proto.RecommendUserProfile;
import com.dating.user.grpc.proto.UserProfileServiceGrpc;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceCandidateClientTest {

    @Mock
    private UserProfileServiceGrpc.UserProfileServiceBlockingStub userProfileStub;

    private UserServiceCandidateClient client;

    @BeforeEach
    void setUp() {
        client = new UserServiceCandidateClient(userProfileStub);
    }

    @Test
    void listDhCandidates_shouldThrowNotImplemented() {
        MatchBizException ex = assertThrows(MatchBizException.class,
                () -> client.listDhCandidates(10001L, 10));
        assertEquals(MatchErrorCode.EXTERNAL_RPC_NOT_IMPLEMENTED, ex.getErrorCode());
    }

    @Test
    void batchGetProfiles_shouldMapRecommendProfile() {
        RecommendUserProfile profile = RecommendUserProfile.newBuilder()
                .setUserId(20001L)
                .setUserType("BH")
                .setBirthDate("1998-01-01")
                .setBio("hello")
                .setProfileScore(85)
                .setAvatarKey("photo/20001/1.jpg")
                .setAvailable(true)
                .build();
        when(userProfileStub.batchGetRecommendProfiles(any())).thenReturn(
                BatchGetRecommendProfilesResponse.newBuilder().addProfiles(profile).build());

        Map<Long, com.dating.match.recommend.CandidateProfile> result =
                client.batchGetProfiles(List.of(20001L));
        assertEquals(1, result.size());
        assertEquals(20001L, result.get(20001L).getUserId());
        assertEquals("hello", result.get(20001L).getBio());
    }
}
