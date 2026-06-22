package com.dating.match.client.grpc;

import com.dating.match.constant.UserTypeConstant;
import com.dating.match.recommend.CandidateProfile;
import com.dating.user.grpc.proto.RecommendUserProfile;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/**
 * user-service proto → match 域 CandidateProfile 映射，隔离外部契约细节。
 */
public final class UserProfileProtoMapper {

    private UserProfileProtoMapper() {
    }

    public static CandidateProfile fromRecommendProfile(RecommendUserProfile profile) {
        CandidateProfile candidate = new CandidateProfile();
        candidate.setUserId(profile.getUserId());
        candidate.setUserType(parseUserType(profile.getUserType()));
        candidate.setBio(profile.getBio());
        candidate.setBeautyScore(profile.getProfileScore());
        candidate.setAge(estimateAge(profile.getBirthDate()));
        candidate.setRace("unknown");
        candidate.setStateCode(profile.getCountryCode());
        candidate.setCity(profile.getCityCode());
        if (candidate.getUserType() == UserTypeConstant.DH) {
            candidate.setDistanceKm(-1D);
        } else {
            candidate.setDistanceKm(10D);
        }
        List<String> photoKeys = new ArrayList<>();
        if (!profile.getAvatarKey().isBlank()) {
            photoKeys.add(profile.getAvatarKey());
        }
        candidate.setPhotoKeys(photoKeys);
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        candidate.setCreatedAt(now.minusDays(7));
        candidate.setLastActiveAt(now.minusHours(6));
        return candidate;
    }

    static int parseUserType(String userType) {
        if (userType == null) {
            return UserTypeConstant.BH;
        }
        return switch (userType.trim().toUpperCase()) {
            case "DH" -> UserTypeConstant.DH;
            default -> UserTypeConstant.BH;
        };
    }

    static int estimateAge(String birthDate) {
        if (birthDate == null || birthDate.isBlank()) {
            return 25;
        }
        try {
            LocalDate birth = LocalDate.parse(birthDate.trim());
            return (int) ChronoUnit.YEARS.between(birth, LocalDate.now(ZoneOffset.UTC));
        } catch (Exception ex) {
            return 25;
        }
    }
}
