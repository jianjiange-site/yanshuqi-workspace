package com.dating.user.service.support;

import com.dating.user.vo.AvatarViewVO;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * AvatarVO 组装：本阶段无缩略图服务，三档 key 临时一致。
 */
@Component
public class AvatarViewConverter {

    /**
     * 从 objectKey 组装 AvatarVO；width/height 来自 statObject，缺失时为 0。
     */
    public AvatarViewVO fromObjectKey(String objectKey, int width, int height) {
        if (!StringUtils.hasText(objectKey)) {
            return null;
        }
        AvatarViewVO avatar = new AvatarViewVO();
        avatar.setOriginalKey(objectKey);
        // 本阶段无图片裁剪，min/mid 与 original 相同，后续可接异步缩略图任务
        avatar.setMinKey(objectKey);
        avatar.setMidKey(objectKey);
        avatar.setWidth(width);
        avatar.setHeight(height);
        return avatar;
    }
}
