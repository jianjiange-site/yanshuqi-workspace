package com.dating.gateway.converter;

import com.dating.gateway.dto.vo.AvatarVO;
import com.dating.gateway.dto.vo.HomeCardVO;
import com.dating.gateway.dto.vo.PresignAvatarUploadVO;
import com.dating.gateway.dto.vo.UserProfileVO;
import com.dating.user.grpc.proto.AvatarView;
import com.dating.user.grpc.proto.ConfirmAvatarUploadResponse;
import com.dating.user.grpc.proto.GetHomeCardProfileResponse;
import com.dating.user.grpc.proto.PresignAvatarUploadResponse;
import com.dating.user.grpc.proto.UserProfileView;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * user proto 响应 → Swagger VO 转换器，风格对齐 {@link com.dating.gateway.adapter.MatchProtoAdapter}。
 */
public final class UserProfileProtoAdapter {

    private UserProfileProtoAdapter() {
    }

    public static UserProfileVO toUserProfileVO(UserProfileView profile) {
        if (profile == null || profile.getUserId() <= 0) {
            return null;
        }
        UserProfileVO vo = new UserProfileVO();
        vo.setUserId(profile.getUserId());
        vo.setNickname(emptyToNull(profile.getNickname()));
        vo.setAge(profile.getAge() > 0 ? profile.getAge() : null);
        vo.setGender(GenderCodec.toSwaggerGender(profile.getGender()));
        vo.setHeight(profile.getHeight() > 0 ? profile.getHeight() : null);
        vo.setBio(emptyToNull(profile.getBio()));
        vo.setOccupation(emptyToNull(profile.getOccupation()));
        vo.setEducation(emptyToNull(profile.getEducation()));
        vo.setLocation(emptyToNull(profile.getLocation()));
        vo.setBirthday(emptyToNull(profile.getBirthday()));
        if (profile.hasAvatar()) {
            vo.setAvatar(toAvatarVO(profile.getAvatar()));
        }
        if (!profile.getInterestsList().isEmpty()) {
            vo.setInterests(new ArrayList<>(profile.getInterestsList()));
        }
        vo.setPending(profile.getPending());
        vo.setRegulationStatus(profile.getRegulationStatus() > 0 ? profile.getRegulationStatus() : null);
        vo.setLastOpenAtMs(profile.getLastOpenAtMs() > 0 ? profile.getLastOpenAtMs() : null);
        return vo;
    }

    public static AvatarVO toAvatarVO(AvatarView avatar) {
        if (avatar == null) {
            return null;
        }
        AvatarVO vo = new AvatarVO();
        vo.setOriginalKey(emptyToNull(avatar.getOriginalKey()));
        vo.setMinKey(emptyToNull(avatar.getMinKey()));
        vo.setMidKey(emptyToNull(avatar.getMidKey()));
        vo.setWidth(avatar.getWidth() > 0 ? avatar.getWidth() : null);
        vo.setHeight(avatar.getHeight() > 0 ? avatar.getHeight() : null);
        return vo;
    }

    public static PresignAvatarUploadVO toPresignAvatarUploadVO(PresignAvatarUploadResponse resp) {
        PresignAvatarUploadVO vo = new PresignAvatarUploadVO();
        vo.setPresignedUrl(emptyToNull(resp.getPresignedUrl()));
        vo.setObjectKey(emptyToNull(resp.getObjectKey()));
        vo.setExpiresAtMs(resp.getExpiresAtMs() > 0 ? resp.getExpiresAtMs() : null);
        return vo;
    }

    public static AvatarVO toAvatarVO(ConfirmAvatarUploadResponse resp) {
        if (resp == null || !resp.hasAvatar()) {
            return null;
        }
        return toAvatarVO(resp.getAvatar());
    }

    public static HomeCardVO toHomeCardVO(GetHomeCardProfileResponse resp) {
        HomeCardVO vo = new HomeCardVO();
        vo.setSelfUserId(resp.getSelfUserId() > 0 ? resp.getSelfUserId() : null);
        if (resp.hasTargetProfile()) {
            vo.setTarget(toUserProfileVO(resp.getTargetProfile()));
        }
        return vo;
    }

    private static String emptyToNull(String value) {
        return StringUtils.hasText(value) ? value : null;
    }
}
