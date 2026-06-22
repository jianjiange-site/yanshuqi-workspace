package com.dating.match.client;

import com.dating.match.constant.UserTypeConstant;
import com.dating.match.recommend.CandidateProfile;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Mock 候选召回：DH 池 240、BH 池 20；测试可通过 {@link #resetPools} 覆盖。
 */
@Component
@ConditionalOnProperty(prefix = "app.match.external", name = "user-client-mode", havingValue = "mock", matchIfMissing = true)
public class MockCandidateClient implements CandidateClient {

    public static final int DEFAULT_DH_POOL_SIZE = 240;
    public static final int DEFAULT_BH_POOL_SIZE = 20;
    public static final long DH_ID_BASE = 30001L;
    public static final long BH_ID_BASE = 20001L;

    private volatile int dhPoolSize = DEFAULT_DH_POOL_SIZE;
    private volatile int bhPoolSize = DEFAULT_BH_POOL_SIZE;
    private final Map<Long, CandidateProfile> profileStore = new ConcurrentHashMap<>();

    public MockCandidateClient() {
        resetPools(DEFAULT_DH_POOL_SIZE, DEFAULT_BH_POOL_SIZE);
    }

    @Override
    public List<CandidateProfile> listDhCandidates(long callerUserId, int limit) {
        return listFromPool(callerUserId, UserTypeConstant.DH, DH_ID_BASE, dhPoolSize, limit);
    }

    @Override
    public List<CandidateProfile> listBhCandidates(long callerUserId, int limit) {
        return listFromPool(callerUserId, UserTypeConstant.BH, BH_ID_BASE, bhPoolSize, limit);
    }

    @Override
    public Map<Long, CandidateProfile> batchGetProfiles(Collection<Long> userIds) {
        Map<Long, CandidateProfile> result = new HashMap<>();
        if (userIds == null) {
            return result;
        }
        for (Long userId : userIds) {
            if (userId == null) {
                continue;
            }
            CandidateProfile profile = profileStore.get(userId);
            if (profile != null) {
                result.put(userId, profile);
            }
        }
        return result;
    }

    public void resetPools(int dhSize, int bhSize) {
        this.dhPoolSize = dhSize;
        this.bhPoolSize = bhSize;
        profileStore.clear();
        for (int i = 0; i < dhSize; i++) {
            long userId = DH_ID_BASE + i;
            profileStore.put(userId, buildProfile(userId, UserTypeConstant.DH, i));
        }
        for (int i = 0; i < bhSize; i++) {
            long userId = BH_ID_BASE + i;
            profileStore.put(userId, buildProfile(userId, UserTypeConstant.BH, i));
        }
    }

    public void putProfile(CandidateProfile profile) {
        profileStore.put(profile.getUserId(), profile);
    }

    private List<CandidateProfile> listFromPool(long callerUserId, int userType, long idBase, int poolSize, int limit) {
        int max = Math.min(limit, poolSize);
        List<CandidateProfile> result = new ArrayList<>(max);
        for (int i = 0; i < poolSize && result.size() < max; i++) {
            long userId = idBase + i;
            if (userId == callerUserId) {
                continue;
            }
            CandidateProfile profile = profileStore.get(userId);
            if (profile != null) {
                result.add(profile);
            }
        }
        return result;
    }

    private static CandidateProfile buildProfile(long userId, int userType, int index) {
        CandidateProfile profile = new CandidateProfile();
        profile.setUserId(userId);
        profile.setUserType(userType);
        profile.setNickname(userType == UserTypeConstant.DH ? "DH-" + userId : "BH-" + userId);
        profile.setAge(22 + (index % 10));
        profile.setPhotoKeys(List.of("photo/" + userId + "/1.jpg"));
        profile.setBio(userType == UserTypeConstant.DH ? "DH bio" : "BH bio");
        profile.setDistanceKm(userType == UserTypeConstant.DH ? -1D : 5.5 + index);
        profile.setStateCode(userType == UserTypeConstant.DH ? "CA" : "NY");
        profile.setCity(userType == UserTypeConstant.DH ? "Los Angeles" : "New York");
        profile.setGender(1);
        profile.setBeautyScore(70 + index % 20);
        profile.setRace("unknown");
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        profile.setCreatedAt(now.minusDays(index));
        profile.setLastActiveAt(userType == UserTypeConstant.BH
                ? now.minusHours(index)
                : now.minusDays(index));
        return profile;
    }
}
