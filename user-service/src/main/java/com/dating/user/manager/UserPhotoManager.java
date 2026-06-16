package com.dating.user.manager;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.dating.user.constant.PhotoType;
import com.dating.user.entity.UserPhotoEntity;
import com.dating.user.mapper.UserPhotoMapper;
import org.springframework.context.annotation.Profile;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

/**
 * 用户照片表数据访问编排，仅封装 user_center.user_photos 单表操作。
 */
@Component
@Profile("!test")
public class UserPhotoManager {

    private final UserPhotoMapper userPhotoMapper;

    /**
     * 构造用户照片 Manager，注入 user_photos 表对应的 Mapper。
     *
     * @param userPhotoMapper 用户照片 Mapper，不能为空
     * @throws IllegalArgumentException 当 userPhotoMapper 为空时
     * 业务约束：仅持有 UserPhotoMapper，仅访问 user_center.user_photos 单表；禁止 JOIN；禁止跨 schema；禁止跨服务数据库访问。
     */
    public UserPhotoManager(UserPhotoMapper userPhotoMapper) {
        this.userPhotoMapper = userPhotoMapper;
    }

    /**
     * 根据照片业务主键查询用户照片记录。
     *
     * @param photoId 照片业务主键，不能为空
     * @return 用户照片实体；不存在时返回 null
     * @throws IllegalArgumentException 当 photoId 为空时
     * @throws DataAccessException 当数据库访问失败时
     * 业务约束：仅访问 user_center.user_photos 单表；禁止 JOIN；禁止跨 schema；禁止跨服务数据库访问。
     */
    public UserPhotoEntity findByPhotoId(Long photoId) {
        LambdaQueryWrapper<UserPhotoEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserPhotoEntity::getPhotoId, photoId);
        return userPhotoMapper.selectOne(wrapper);
    }

    /**
     * 根据用户业务主键与 object key 查询照片记录。
     *
     * @param userId    用户业务主键
     * @param objectKey object key
     * @return 照片实体；不存在时返回 null
     * @throws IllegalArgumentException 当 userId 或 objectKey 为空时
     * @throws DataAccessException 当数据库访问失败时
     * 业务约束：仅访问 user_center.user_photos 单表；禁止 JOIN；禁止跨 schema；禁止跨服务数据库访问。
     */
    public UserPhotoEntity findByUserIdAndObjectKey(Long userId, String objectKey) {
        if (userId == null || userId <= 0 || !StringUtils.hasText(objectKey)) {
            throw new IllegalArgumentException("userId 或 objectKey 非法");
        }
        LambdaQueryWrapper<UserPhotoEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserPhotoEntity::getUserId, userId)
                .eq(UserPhotoEntity::getObjectKey, objectKey.trim());
        return userPhotoMapper.selectOne(wrapper);
    }

    /**
     * 根据用户业务主键和照片类型查询用户照片列表。
     *
     * @param userId    用户业务主键，不能为空
     * @param photoType 照片类型，如 AVATAR、ALBUM，不能为空
     * @return 用户照片列表，无记录时返回空列表
     * @throws IllegalArgumentException 当 userId 或 photoType 为空时
     * @throws DataAccessException 当数据库访问失败时
     * 业务约束：仅访问 user_center.user_photos 单表；禁止 JOIN；禁止跨 schema；禁止跨服务数据库访问。
     */
    public List<UserPhotoEntity> listByUserIdAndPhotoType(Long userId, String photoType) {
        LambdaQueryWrapper<UserPhotoEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserPhotoEntity::getUserId, userId)
                .eq(UserPhotoEntity::getPhotoType, photoType)
                .orderByAsc(UserPhotoEntity::getSortOrder);
        return userPhotoMapper.selectList(wrapper);
    }

    /**
     * 按用户与可选类型查询照片列表，支持过滤禁用记录。
     *
     * @param userId           用户业务主键
     * @param photoType        照片类型，可为 null 表示不过滤
     * @param includeDisabled  是否包含 enabled=0 的记录
     * @return 照片列表，按 sort_order 升序
     * @throws IllegalArgumentException 当 userId 非法时
     * @throws DataAccessException 当数据库访问失败时
     * 业务约束：仅访问 user_center.user_photos 单表；禁止 JOIN；禁止跨 schema；禁止跨服务数据库访问。
     */
    public List<UserPhotoEntity> listByUserIdAndType(Long userId, String photoType, boolean includeDisabled) {
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("userId 非法");
        }
        LambdaQueryWrapper<UserPhotoEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserPhotoEntity::getUserId, userId);
        if (StringUtils.hasText(photoType)) {
            wrapper.eq(UserPhotoEntity::getPhotoType, photoType.trim().toUpperCase());
        }
        if (!includeDisabled) {
            wrapper.eq(UserPhotoEntity::getEnabled, 1);
        }
        wrapper.orderByAsc(UserPhotoEntity::getSortOrder);
        return userPhotoMapper.selectList(wrapper);
    }

    /**
     * 统计用户指定类型的启用照片数量。
     *
     * @param userId    用户业务主键
     * @param photoType 照片类型
     * @return 启用照片数量
     * @throws IllegalArgumentException 当 userId 或 photoType 为空时
     * @throws DataAccessException 当数据库访问失败时
     * 业务约束：仅访问 user_center.user_photos 单表；禁止 JOIN；禁止跨 schema；禁止跨服务数据库访问。
     */
    public long countEnabledByUserIdAndType(Long userId, String photoType) {
        if (userId == null || userId <= 0 || !StringUtils.hasText(photoType)) {
            throw new IllegalArgumentException("userId 或 photoType 非法");
        }
        LambdaQueryWrapper<UserPhotoEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserPhotoEntity::getUserId, userId)
                .eq(UserPhotoEntity::getPhotoType, photoType.trim().toUpperCase())
                .eq(UserPhotoEntity::getEnabled, 1);
        return userPhotoMapper.selectCount(wrapper);
    }

    /**
     * 禁用用户当前启用的头像记录。
     *
     * @param userId 用户业务主键
     * @throws IllegalArgumentException 当 userId 非法时
     * @throws DataAccessException 当数据库访问失败时
     * 业务约束：仅访问 user_center.user_photos 单表；禁止 JOIN；禁止跨 schema；禁止跨服务数据库访问。
     */
    public void disableCurrentAvatar(Long userId) {
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("userId 非法");
        }
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        LambdaUpdateWrapper<UserPhotoEntity> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(UserPhotoEntity::getUserId, userId)
                .eq(UserPhotoEntity::getPhotoType, PhotoType.AVATAR.name())
                .eq(UserPhotoEntity::getEnabled, 1)
                .set(UserPhotoEntity::getEnabled, 0)
                .set(UserPhotoEntity::getUpdatedAt, now);
        userPhotoMapper.update(null, wrapper);
    }

    /**
     * 创建用户照片记录。
     *
     * @param photoId      照片业务主键
     * @param userId       用户业务主键
     * @param photoType    照片类型
     * @param objectKey    object key
     * @param sortOrder    排序值
     * @param reviewStatus 审核状态
     * @param enabled      是否启用
     * @throws IllegalArgumentException 当关键参数非法时
     * @throws DataAccessException 当数据库访问失败或唯一约束冲突时
     * 业务约束：仅访问 user_center.user_photos 单表；禁止 JOIN；禁止跨 schema；禁止跨服务数据库访问。
     */
    public void createPhoto(long photoId,
                            long userId,
                            String photoType,
                            String objectKey,
                            int sortOrder,
                            String reviewStatus,
                            int enabled) {
        if (photoId <= 0 || userId <= 0 || !StringUtils.hasText(photoType) || !StringUtils.hasText(objectKey)) {
            throw new IllegalArgumentException("照片参数非法");
        }
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        UserPhotoEntity entity = new UserPhotoEntity();
        entity.setPhotoId(photoId);
        entity.setUserId(userId);
        entity.setPhotoType(photoType.trim().toUpperCase());
        entity.setObjectKey(objectKey.trim());
        entity.setSortOrder(sortOrder);
        entity.setReviewStatus(reviewStatus);
        entity.setEnabled(enabled);
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);
        userPhotoMapper.insert(entity);
    }

    /**
     * 更新照片启用状态与排序。
     *
     * @param entity    待更新实体，id 不能为空
     * @param enabled   是否启用
     * @param sortOrder 排序值
     * @throws IllegalArgumentException 当 entity 或 id 为空时
     * @throws DataAccessException 当数据库访问失败时
     * 业务约束：仅访问 user_center.user_photos 单表；禁止 JOIN；禁止跨 schema；禁止跨服务数据库访问。
     */
    public void updatePhotoEnabledOrSort(UserPhotoEntity entity, int enabled, int sortOrder) {
        if (entity == null || entity.getId() == null) {
            throw new IllegalArgumentException("照片实体或 id 不能为空");
        }
        entity.setEnabled(enabled);
        entity.setSortOrder(sortOrder);
        entity.setUpdatedAt(OffsetDateTime.now(ZoneOffset.UTC));
        userPhotoMapper.updateById(entity);
    }

    /**
     * 插入用户照片记录。
     *
     * @param entity 待插入的用户照片实体，不能为空
     * @return 影响行数，成功时为 1
     * @throws IllegalArgumentException 当 entity 为空时
     * @throws DataAccessException 当数据库访问失败或唯一约束冲突时
     * 业务约束：仅访问 user_center.user_photos 单表；禁止 JOIN；禁止跨 schema；禁止跨服务数据库访问。
     */
    public int insert(UserPhotoEntity entity) {
        return userPhotoMapper.insert(entity);
    }

    /**
     * 根据物理主键更新用户照片记录。
     *
     * @param entity 待更新的用户照片实体，不能为空且 id 不能为空
     * @return 影响行数，未命中记录时为 0
     * @throws IllegalArgumentException 当 entity 为空或 id 为空时
     * @throws DataAccessException 当数据库访问失败时
     * 业务约束：仅访问 user_center.user_photos 单表；禁止 JOIN；禁止跨 schema；禁止跨服务数据库访问。
     */
    public int updateById(UserPhotoEntity entity) {
        return userPhotoMapper.updateById(entity);
    }
}
