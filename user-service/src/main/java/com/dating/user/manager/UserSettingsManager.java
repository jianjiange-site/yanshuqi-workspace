package com.dating.user.manager;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.dating.user.entity.UserSettingsEntity;
import com.dating.user.mapper.UserSettingsMapper;
import org.springframework.context.annotation.Profile;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;

/**
 * 用户设置表数据访问编排，仅封装 user_center.user_settings 单表操作。
 */
@Component
@Profile("!test")
public class UserSettingsManager {

    private final UserSettingsMapper userSettingsMapper;

    /**
     * 构造用户设置 Manager，注入 user_settings 表对应的 Mapper。
     *
     * @param userSettingsMapper 用户设置 Mapper，不能为空
     * @throws IllegalArgumentException 当 userSettingsMapper 为空时
     * 业务约束：仅持有 UserSettingsMapper，仅访问 user_center.user_settings 单表；禁止 JOIN；禁止跨 schema；禁止跨服务数据库访问。
     */
    public UserSettingsManager(UserSettingsMapper userSettingsMapper) {
        this.userSettingsMapper = userSettingsMapper;
    }

    /**
     * 根据用户业务主键查询用户设置记录。
     *
     * @param userId 用户业务主键，不能为空
     * @return 用户设置实体；不存在时返回 null
     * @throws IllegalArgumentException 当 userId 为空时
     * @throws DataAccessException 当数据库访问失败时
     * 业务约束：仅访问 user_center.user_settings 单表；禁止 JOIN；禁止跨 schema；禁止跨服务数据库访问。
     */
    public UserSettingsEntity findByUserId(Long userId) {
        // 1. 构造单表等值查询条件
        LambdaQueryWrapper<UserSettingsEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserSettingsEntity::getUserId, userId);
        // 2. 执行单表查询
        return userSettingsMapper.selectOne(wrapper);
    }

    /**
     * 根据设置业务主键查询用户设置记录。
     *
     * @param settingId 设置业务主键，不能为空
     * @return 用户设置实体；不存在时返回 null
     * @throws IllegalArgumentException 当 settingId 为空时
     * @throws DataAccessException 当数据库访问失败时
     * 业务约束：仅访问 user_center.user_settings 单表；禁止 JOIN；禁止跨 schema；禁止跨服务数据库访问。
     */
    public UserSettingsEntity findBySettingId(Long settingId) {
        // 1. 构造单表等值查询条件
        LambdaQueryWrapper<UserSettingsEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserSettingsEntity::getSettingId, settingId);
        // 2. 执行单表查询
        return userSettingsMapper.selectOne(wrapper);
    }

    /**
     * 插入用户设置记录。
     *
     * @param entity 待插入的用户设置实体，不能为空
     * @return 影响行数，成功时为 1
     * @throws IllegalArgumentException 当 entity 为空时
     * @throws DataAccessException 当数据库访问失败或唯一约束冲突时
     * 业务约束：仅访问 user_center.user_settings 单表；禁止 JOIN；禁止跨 schema；禁止跨服务数据库访问；不编排注册初始化设置等业务流程。
     */
    public int insert(UserSettingsEntity entity) {
        // 1. 执行单表插入
        return userSettingsMapper.insert(entity);
    }

    /**
     * 根据物理主键更新用户设置记录。
     *
     * @param entity 待更新的用户设置实体，不能为空且 id 不能为空
     * @return 影响行数，未命中记录时为 0
     * @throws IllegalArgumentException 当 entity 为空或 id 为空时
     * @throws DataAccessException 当数据库访问失败时
     * 业务约束：仅访问 user_center.user_settings 单表；禁止 JOIN；禁止跨 schema；禁止跨服务数据库访问。
     */
    public int updateById(UserSettingsEntity entity) {
        // 1. 按物理主键执行单表更新
        return userSettingsMapper.updateById(entity);
    }
}
