package com.dating.gateway.service;

import com.dating.gateway.dto.ConfirmAvatarReq;
import com.dating.gateway.dto.PresignAvatarReq;
import com.dating.gateway.dto.vo.AvatarVO;
import com.dating.gateway.dto.vo.PresignAvatarUploadVO;

/**
 * Upload BFF：头像 presign / confirm 代理，不接触对象存储凭证与文件流。
 */
public interface UploadBffService {

    PresignAvatarUploadVO presignAvatar(long callerUserId, PresignAvatarReq req);

    AvatarVO confirmAvatar(long callerUserId, ConfirmAvatarReq req);
}
