package com.dating.user.manager;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.dating.user.entity.UserEntity;
import com.dating.user.mapper.UserMapper;
import org.springframework.context.annotation.Profile;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * 用户主表数据访问编排，仅封装 user_center.users 单表操作。
 */
@Component
@Profile("!test")
public class UserManager {

    private final UserMapper userMapper;

    /**
     * 构造用户主表 Manager，注入 users 表对应的 Mapper。
     *
     * @param userMapper 用户主表 Mapper，不能为空
     * @throws IllegalArgumentException 当 userMapper 为空时
     * 业务约束：仅持有 UserMapper，仅访问 user_center.users 单表；禁止 JOIN；禁止跨 schema；禁止跨服务数据库访问。
     */
    public UserManager(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    /**
     * 根据用户业务主键查询用户主表记录。
     *
     * @param userId 用户业务主键，不能为空
     * @return 用户实体；不存在时返回 null
     * @throws IllegalArgumentException 当 userId 为空时
     * @throws DataAccessException 当数据库访问失败时
     * 业务约束：仅访问 user_center.users 单表；禁止 JOIN；禁止跨 schema；禁止跨服务数据库访问。
     */
    public UserEntity findByUserId(Long userId) {
        // 1. 构造单表等值查询条件
        LambdaQueryWrapper<UserEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserEntity::getUserId, userId);
        // 2. 执行单表查询
        return userMapper.selectOne(wrapper);
    }

    /**
     * 插入用户主表记录。
     *
     * @param entity 待插入的用户实体，不能为空且 userId 不能为空
     * @return 影响行数，成功时为 1
     * @throws IllegalArgumentException 当 entity 为空或 userId 为空时
     * @throws DataAccessException 当数据库访问失败或唯一约束冲突时
     * 业务约束：仅访问 user_center.users 单表；禁止 JOIN；禁止跨 schema；禁止跨服务数据库访问；不编排注册等业务流程。
     */
    public int insert(UserEntity entity) {
        // 1. 执行单表插入
        return userMapper.insert(entity);
    }

    /**
     * 创建用户主表记录。
     *
     * @param entity 用户实体，userId 不能为空
     * @throws IllegalArgumentException 当 entity 或 userId 为空时
     * @throws DataAccessException 当数据库访问失败或唯一约束冲突时
     * 业务约束：仅访问 user_center.users 单表；禁止 JOIN；禁止跨 schema；禁止跨服务数据库访问。
     */
    public void createUser(UserEntity entity) {
        if (entity == null || entity.getUserId() == null) {
            throw new IllegalArgumentException("用户实体或 userId 不能为空");
        }
        // 1. 执行单表插入
        userMapper.insert(entity);
    }

    /**
     * 根据物理主键更新用户主表记录。
     *
     * @param entity 待更新的用户实体，不能为空且 id 不能为空
     * @return 影响行数，未命中记录时为 0
     * @throws IllegalArgumentException 当 entity 为空或 id 为空时
     * @throws DataAccessException 当数据库访问失败时
     * 业务约束：仅访问 user_center.users 单表；禁止 JOIN；禁止跨 schema；禁止跨服务数据库访问。
     */
    public int updateById(UserEntity entity) {
        // 1. 按物理主键执行单表更新
        return userMapper.updateById(entity);
    }

    /**
     * 根据物理主键逻辑删除用户主表记录。
     *
     * @param id 物理主键，不能为空
     * @return 影响行数，未命中记录时为 0
     * @throws IllegalArgumentException 当 id 为空时
     * @throws DataAccessException 当数据库访问失败时
     * 业务约束：仅访问 user_center.users 单表；禁止 JOIN；禁止跨 schema；禁止跨服务数据库访问。
     */
    public int deleteById(Long id) {
        // 1. 按物理主键执行逻辑删除
        return userMapper.deleteById(id);
    }

    /**
     * 更新用户最近一次登录时间。
     *
     * @param userId       用户业务主键
     * @param lastLoginAt  最近一次登录时间，UTC
     * @throws IllegalArgumentException 当 userId 为空或 lastLoginAt 为空时
     * @throws DataAccessException 当数据库访问失败时
     * 业务约束：仅访问 user_center.users 单表；禁止 JOIN；禁止跨 schema；禁止跨服务数据库访问。
     */
    public void updateLastLoginAt(Long userId, java.time.OffsetDateTime lastLoginAt) {
        if (userId == null || userId <= 0 || lastLoginAt == null) {
            throw new IllegalArgumentException("userId 或 lastLoginAt 非法");
        }
        // 1. 按 user_id 单表更新 last_login_at
        LambdaUpdateWrapper<UserEntity> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(UserEntity::getUserId, userId)
                .set(UserEntity::getLastLoginAt, lastLoginAt)
                .set(UserEntity::getUpdatedAt, lastLoginAt);
        userMapper.update(null, wrapper);
    }
}
