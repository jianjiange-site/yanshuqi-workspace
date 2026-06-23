package com.dating.gateway.converter;

import com.dating.gateway.dto.ConfirmAvatarReq;
import com.dating.gateway.dto.PresignAvatarReq;
import com.dating.gateway.dto.UpdateProfileReq;
import com.dating.gateway.dto.UpsertOnboardingReq;
import com.dating.user.grpc.proto.ConfirmAvatarUploadRequest;
import com.dating.user.grpc.proto.GetHomeCardProfileRequest;
import com.dating.user.grpc.proto.PresignAvatarUploadRequest;
import com.dating.user.grpc.proto.UpdateProfileRequest;
import com.dating.user.grpc.proto.UpsertOnboardingRequest;
import org.springframework.util.StringUtils;

/**
 * REST DTO → user proto request 手写 builder；null 字段不写入，避免空字符串覆盖下游。
 */
public final class UserProfileReqBuilder {

    private UserProfileReqBuilder() {
    }

    /**
     * 构建 onboarding gRPC 请求，callerUserId 来自 JWT，不允许前端传入。
     */
    public static UpsertOnboardingRequest buildUpsertOnboarding(long callerUserId, UpsertOnboardingReq req) {
        UpsertOnboardingRequest.Builder builder = UpsertOnboardingRequest.newBuilder()
                .setUserId(callerUserId);
        if (req == null) {
            return builder.build();
        }
        if (req.getNickname() != null) {
            builder.setNickname(req.getNickname());
        }
        if (req.getGender() != null) {
            builder.setGender(GenderCodec.toProtoGender(req.getGender()));
        }
        if (req.getBirthday() != null) {
            builder.setBirthday(req.getBirthday());
        }
        if (req.getAge() != null) {
            builder.setAge(req.getAge());
        }
        if (req.getHeight() != null) {
            builder.setHeight(req.getHeight());
        }
        if (req.getBio() != null) {
            builder.setBio(req.getBio());
        }
        if (req.getOccupation() != null) {
            builder.setOccupation(req.getOccupation());
        }
        if (req.getEducation() != null) {
            builder.setEducation(req.getEducation());
        }
        if (req.getLocation() != null) {
            builder.setLocation(req.getLocation());
        }
        if (req.getDefaultAvatarObjectKey() != null) {
            builder.setDefaultAvatarObjectKey(req.getDefaultAvatarObjectKey());
        }
        return builder.build();
    }

    /**
     * 构建日常资料更新请求；禁止写入 gender / birthday。
     */
    public static UpdateProfileRequest buildUpdateProfile(long callerUserId, UpdateProfileReq req) {
        UpdateProfileRequest.Builder builder = UpdateProfileRequest.newBuilder()
                .setUserId(callerUserId);
        if (req == null) {
            return builder.build();
        }
        if (req.getNickname() != null) {
            builder.setNickname(req.getNickname());
        }
        if (req.getAge() != null) {
            builder.setAge(req.getAge());
        }
        if (req.getHeight() != null) {
            builder.setHeight(req.getHeight());
        }
        if (req.getBio() != null) {
            builder.setBio(req.getBio());
        }
        if (req.getOccupation() != null) {
            builder.setOccupation(req.getOccupation());
        }
        if (req.getEducation() != null) {
            builder.setEducation(req.getEducation());
        }
        if (req.getLocation() != null) {
            builder.setLocation(req.getLocation());
        }
        return builder.build();
    }

    public static PresignAvatarUploadRequest buildPresignAvatar(long callerUserId, String ext, long expectedSizeBytes) {
        return PresignAvatarUploadRequest.newBuilder()
                .setUserId(callerUserId)
                .setExt(ext)
                .setExpectedSizeBytes(expectedSizeBytes)
                .build();
    }

    public static ConfirmAvatarUploadRequest buildConfirmAvatar(long callerUserId, ConfirmAvatarReq req) {
        return ConfirmAvatarUploadRequest.newBuilder()
                .setUserId(callerUserId)
                .setObjectKey(req.getObjectKey())
                .build();
    }

    public static GetHomeCardProfileRequest buildGetHomeCard(long callerUserId, long targetUserId) {
        return GetHomeCardProfileRequest.newBuilder()
                .setSelfUserId(callerUserId)
                .setTargetUserId(targetUserId)
                .build();
    }

    /**
     * 规范化上传扩展名：去点前缀、转小写。
     */
    public static String normalizeExt(String ext) {
        if (!StringUtils.hasText(ext)) {
            return "";
        }
        String normalized = ext.trim().toLowerCase();
        if (normalized.startsWith(".")) {
            normalized = normalized.substring(1);
        }
        return normalized;
    }
}
