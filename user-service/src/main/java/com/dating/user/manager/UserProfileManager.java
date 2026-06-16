package com.dating.user.manager;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.dating.user.entity.UserProfileEntity;
import com.dating.user.mapper.UserProfileMapper;
import org.springframework.context.annotation.Profile;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * 用户资料表数据访问编排，仅封装 user_center.user_profiles 单表操作。
 */
@Component
@Profile("!test")
public class UserProfileManager {

    private final UserProfileMapper userProfileMapper;

    /**
     * 构造用户资料 Manager，注入 user_profiles 表对应的 Mapper。
     *
     * @param userProfileMapper 用户资料 Mapper，不能为空
     * @throws IllegalArgumentException 当 userProfileMapper 为空时
     * 业务约束：仅持有 UserProfileMapper，仅访问 user_center.user_profiles 单表；禁止 JOIN；禁止跨 schema；禁止跨服务数据库访问。
     */
    public UserProfileManager(UserProfileMapper userProfileMapper) {
        this.userProfileMapper = userProfileMapper;
    }

    /**
     * 根据用户业务主键查询用户资料记录。
     *
     * @param userId 用户业务主键，不能为空
     * @return 用户资料实体；不存在时返回 null
     * @throws IllegalArgumentException 当 userId 为空时
     * @throws DataAccessException 当数据库访问失败时
     * 业务约束：仅访问 user_center.user_profiles 单表；禁止 JOIN；禁止跨 schema；禁止跨服务数据库访问。
     */
    public UserProfileEntity findByUserId(Long userId) {
        // 1. 构造单表等值查询条件
        LambdaQueryWrapper<UserProfileEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserProfileEntity::getUserId, userId);
        // 2. 执行单表查询
        return userProfileMapper.selectOne(wrapper);
    }

    /**
     * 根据资料业务主键查询用户资料记录。
     *
     * @param profileId 资料业务主键，不能为空
     * @return 用户资料实体；不存在时返回 null
     * @throws IllegalArgumentException 当 profileId 为空时
     * @throws DataAccessException 当数据库访问失败时
     * 业务约束：仅访问 user_center.user_profiles 单表；禁止 JOIN；禁止跨 schema；禁止跨服务数据库访问。
     */
    public UserProfileEntity findByProfileId(Long profileId) {
        // 1. 构造单表等值查询条件
        LambdaQueryWrapper<UserProfileEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserProfileEntity::getProfileId, profileId);
        // 2. 执行单表查询
        return userProfileMapper.selectOne(wrapper);
    }

    /**
     * 插入用户资料记录。
     *
     * @param entity 待插入的用户资料实体，不能为空
     * @return 影响行数，成功时为 1
     * @throws IllegalArgumentException 当 entity 为空时
     * @throws DataAccessException 当数据库访问失败或唯一约束冲突时
     * 业务约束：仅访问 user_center.user_profiles 单表；禁止 JOIN；禁止跨 schema；禁止跨服务数据库访问；不编排资料维护等业务流程。
     */
    public int insert(UserProfileEntity entity) {
        // 1. 执行单表插入
        return userProfileMapper.insert(entity);
    }

    /**
     * 创建默认用户资料记录。
     *
     * @param profileId 资料业务主键
     * @param userId    用户业务主键
     * @throws IllegalArgumentException 当 profileId 或 userId 非法时
     * @throws DataAccessException 当数据库访问失败或唯一约束冲突时
     * 业务约束：仅访问 user_center.user_profiles 单表；禁止 JOIN；禁止跨 schema；禁止跨服务数据库访问。
     */
    public void createDefaultProfile(long profileId, long userId) {
        if (profileId <= 0 || userId <= 0) {
            throw new IllegalArgumentException("profileId 或 userId 非法");
        }
        UserProfileEntity entity = new UserProfileEntity();
        entity.setProfileId(profileId);
        entity.setUserId(userId);
        entity.setProfileScore(0);
        entity.setProfileCompleted(0);
        // 1. 执行单表插入
        userProfileMapper.insert(entity);
    }

    /**
     * 根据物理主键更新用户资料记录。
     *
     * @param entity 待更新的用户资料实体，不能为空且 id 不能为空
     * @return 影响行数，未命中记录时为 0
     * @throws IllegalArgumentException 当 entity 为空或 id 为空时
     * @throws DataAccessException 当数据库访问失败时
     * 业务约束：仅访问 user_center.user_profiles 单表；禁止 JOIN；禁止跨 schema；禁止跨服务数据库访问。
     */
    public int updateById(UserProfileEntity entity) {
        // 1. 按物理主键执行单表更新
        return userProfileMapper.updateById(entity);
    }

    /**
     * 更新用户资料记录，仅更新允许维护的基础字段，不修改 avatar_key。
     *
     * @param entity 待更新的资料实体，id 不能为空
     * @throws IllegalArgumentException 当 entity 或 id 为空时
     * @throws DataAccessException 当数据库访问失败时
     * 业务约束：仅访问 user_center.user_profiles 单表；禁止 JOIN；禁止跨 schema；禁止跨服务数据库访问。
     */
    public void updateProfile(UserProfileEntity entity) {
        if (entity == null || entity.getId() == null) {
            throw new IllegalArgumentException("资料实体或 id 不能为空");
        }
        entity.setUpdatedAt(OffsetDateTime.now(ZoneOffset.UTC));
        // 1. 按物理主键执行单表更新
        userProfileMapper.updateById(entity);
    }

    /**
     * 更新用户头像 object key。
     *
     * @param userId    用户业务主键
     * @param avatarKey 头像 object key
     * @throws IllegalArgumentException 当 userId 或 avatarKey 为空时
     * @throws DataAccessException 当数据库访问失败时
     * 业务约束：仅访问 user_center.user_profiles 单表；禁止 JOIN；禁止跨 schema；禁止跨服务数据库访问。
     */
    public void updateAvatarKey(Long userId, String avatarKey) {
        if (userId == null || userId <= 0 || !StringUtils.hasText(avatarKey)) {
            throw new IllegalArgumentException("userId 或 avatarKey 非法");
        }
        UserProfileEntity entity = findByUserId(userId);
        if (entity == null || entity.getId() == null) {
            throw new IllegalArgumentException("用户资料不存在");
        }
        entity.setAvatarKey(avatarKey.trim());
        entity.setUpdatedAt(OffsetDateTime.now(ZoneOffset.UTC));
        userProfileMapper.updateById(entity);
    }

    /**
     * 根据用户业务主键列表批量查询用户资料记录。
     *
     * @param userIds 用户业务主键集合，不能为空
     * @return 用户资料实体列表，无记录时返回空列表
     * @throws IllegalArgumentException 当 userIds 为空时
     * @throws DataAccessException 当数据库访问失败时
     * 业务约束：仅访问 user_center.user_profiles 单表；禁止 JOIN；禁止跨 schema；禁止跨服务数据库访问。
     */
    public List<UserProfileEntity> listByUserIds(Collection<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Collections.emptyList();
        }
        LambdaQueryWrapper<UserProfileEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(UserProfileEntity::getUserId, userIds);
        return userProfileMapper.selectList(wrapper);
    }
}
