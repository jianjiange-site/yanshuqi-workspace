package com.dating.match.client;

import com.dating.match.recommend.CandidateProfile;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 推荐候选召回防腐层接口；实现由 mock 或 user-service gRPC 提供。
 */
public interface CandidateClient {

    List<CandidateProfile> listDhCandidates(long callerUserId, int limit);

    List<CandidateProfile> listBhCandidates(long callerUserId, int limit);

    Map<Long, CandidateProfile> batchGetProfiles(Collection<Long> userIds);
}
