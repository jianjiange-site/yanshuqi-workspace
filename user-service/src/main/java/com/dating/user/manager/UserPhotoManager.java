package com.dating.user.manager;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.dating.user.entity.UserPhotoEntity;
import com.dating.user.mapper.UserPhotoMapper;
import org.springframework.context.annotation.Profile;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;

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
        // 1. 构造单表等值查询条件
        LambdaQueryWrapper<UserPhotoEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserPhotoEntity::getPhotoId, photoId);
        // 2. 执行单表查询
        return userPhotoMapper.selectOne(wrapper);
    }

    /**
     * 根据用户业务主键和照片类型查询用户照片列表。
     *
     * @param userId 用户业务主键，不能为空
     * @param photoType 照片类型，如 AVATAR、ALBUM，不能为空
     * @return 用户照片列表，无记录时返回空列表
     * @throws IllegalArgumentException 当 userId 或 photoType 为空时
     * @throws DataAccessException 当数据库访问失败时
     * 业务约束：仅访问 user_center.user_photos 单表；禁止 JOIN；禁止跨 schema；禁止跨服务数据库访问；不关联 user_profiles 等其它表。
     */
    public List<UserPhotoEntity> listByUserIdAndPhotoType(Long userId, String photoType) {
        // 1. 构造单表复合等值查询条件
        LambdaQueryWrapper<UserPhotoEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserPhotoEntity::getUserId, userId)
                .eq(UserPhotoEntity::getPhotoType, photoType)
                .orderByAsc(UserPhotoEntity::getSortOrder);
        // 2. 执行单表列表查询
        return userPhotoMapper.selectList(wrapper);
    }

    /**
     * 插入用户照片记录。
     *
     * @param entity 待插入的用户照片实体，不能为空
     * @return 影响行数，成功时为 1
     * @throws IllegalArgumentException 当 entity 为空时
     * @throws DataAccessException 当数据库访问失败或唯一约束冲突时
     * 业务约束：仅访问 user_center.user_photos 单表；禁止 JOIN；禁止跨 schema；禁止跨服务数据库访问；不编排头像绑定等业务流程。
     */
    public int insert(UserPhotoEntity entity) {
        // 1. 执行单表插入
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
        // 1. 按物理主键执行单表更新
        return userPhotoMapper.updateById(entity);
    }
}
