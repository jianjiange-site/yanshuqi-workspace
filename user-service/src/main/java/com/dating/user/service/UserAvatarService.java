package com.dating.user.service;

import com.dating.user.dto.ConfirmAvatarUploadCommand;
import com.dating.user.dto.PresignAvatarUploadCommand;
import com.dating.user.vo.AvatarViewVO;
import com.dating.user.vo.PresignAvatarUploadResult;

/**
 * 用户头像上传业务服务。
 */
public interface UserAvatarService {

    /**
     * 签发头像 PUT presigned URL，不落库。
     */
    PresignAvatarUploadResult presignAvatarUpload(PresignAvatarUploadCommand command);

    /**
     * 确认头像上传，statObject 后写入 user_photos 并更新 avatar_key。
     */
    AvatarViewVO confirmAvatarUpload(ConfirmAvatarUploadCommand command);
}
