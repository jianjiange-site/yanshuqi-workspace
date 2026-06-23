package com.dating.gateway.converter;

import com.dating.gateway.dto.UpdateProfileReq;
import com.dating.gateway.dto.UpsertOnboardingReq;
import com.dating.gateway.dto.vo.AvatarVO;
import com.dating.gateway.dto.vo.UserProfileVO;
import com.dating.user.grpc.proto.AvatarView;
import com.dating.user.grpc.proto.UpdateProfileRequest;
import com.dating.user.grpc.proto.UpsertOnboardingRequest;
import com.dating.user.grpc.proto.UserProfileView;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UserProfileProtoAdapterTest {

    @Test
    void genderCodec_shouldConvertSwaggerToProtoAndBack() {
        assertEquals("MALE", GenderCodec.toProtoGender(1));
        assertEquals("FEMALE", GenderCodec.toProtoGender(2));
        assertEquals("UNKNOWN", GenderCodec.toProtoGender(0));
        assertEquals(1, GenderCodec.toSwaggerGender("MALE"));
        assertEquals(2, GenderCodec.toSwaggerGender("FEMALE"));
        assertEquals(0, GenderCodec.toSwaggerGender("OTHER"));
        assertEquals(0, GenderCodec.toSwaggerGender(""));
    }

    @Test
    void genderCodec_invalidSwaggerValue_shouldThrow() {
        assertThrows(com.dating.gateway.exception.GatewayBizException.class,
                () -> GenderCodec.toProtoGender(99));
    }

    @Test
    void toAvatarVO_shouldMapKeysAndDimensions() {
        AvatarView avatar = AvatarView.newBuilder()
                .setOriginalKey("avatars/u1/orig.jpg")
                .setMinKey("avatars/u1/min.jpg")
                .setMidKey("avatars/u1/mid.jpg")
                .setWidth(800)
                .setHeight(600)
                .build();

        AvatarVO vo = UserProfileProtoAdapter.toAvatarVO(avatar);

        assertEquals("avatars/u1/orig.jpg", vo.getOriginalKey());
        assertEquals("avatars/u1/min.jpg", vo.getMinKey());
        assertEquals("avatars/u1/mid.jpg", vo.getMidKey());
        assertEquals(800, vo.getWidth());
        assertEquals(600, vo.getHeight());
    }

    @Test
    void toUserProfileVO_shouldMapFieldsAndConvertGender() {
        UserProfileView profile = UserProfileView.newBuilder()
                .setUserId(10001L)
                .setNickname("Alice")
                .setAge(26)
                .setGender("FEMALE")
                .setHeight(165)
                .setBio("hello")
                .setBirthday("1998-05-20")
                .setAvatar(AvatarView.newBuilder().setOriginalKey("key.jpg").build())
                .addInterests("music")
                .setPending(false)
                .setRegulationStatus(1)
                .setLastOpenAtMs(1234567890L)
                .build();

        UserProfileVO vo = UserProfileProtoAdapter.toUserProfileVO(profile);

        assertEquals(10001L, vo.getUserId());
        assertEquals("Alice", vo.getNickname());
        assertEquals(26, vo.getAge());
        assertEquals(2, vo.getGender());
        assertEquals("key.jpg", vo.getAvatar().getOriginalKey());
        assertEquals(1, vo.getInterests().size());
    }

    @Test
    void reqBuilder_nullFieldsShouldNotOverwriteProtoDefaults() {
        UpsertOnboardingReq req = new UpsertOnboardingReq();
        req.setNickname("Bob");

        UpsertOnboardingRequest onboarding = UserProfileReqBuilder.buildUpsertOnboarding(20002L, req);
        assertEquals(20002L, onboarding.getUserId());
        assertEquals("Bob", onboarding.getNickname());
        assertFalse(onboarding.hasAge());
        assertEquals("", onboarding.getGender());

        UpdateProfileReq updateReq = new UpdateProfileReq();
        updateReq.setBio("updated");
        UpdateProfileRequest update = UserProfileReqBuilder.buildUpdateProfile(20002L, updateReq);
        assertEquals("updated", update.getBio());
        assertEquals("", update.getGender());
        assertEquals("", update.getBirthDate());
        assertFalse(update.hasAge());
    }

    @Test
    void reqBuilder_onboardingShouldSetOptionalNumericFields() {
        UpsertOnboardingReq req = new UpsertOnboardingReq();
        req.setAge(25);
        req.setHeight(170);
        req.setGender(1);

        UpsertOnboardingRequest onboarding = UserProfileReqBuilder.buildUpsertOnboarding(1L, req);
        assertTrue(onboarding.hasAge());
        assertTrue(onboarding.hasHeight());
        assertEquals("MALE", onboarding.getGender());
    }
}
