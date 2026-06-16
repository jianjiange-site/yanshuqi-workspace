package com.dating.user.manager;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.dating.user.entity.UserAuthIdentityEntity;
import com.dating.user.mapper.UserAuthIdentityMapper;
import org.springframework.context.annotation.Profile;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

/**
 * 用户登录凭证表数据访问编排，仅封装 user_center.user_auth_identities 单表操作。
 */
@Component
@Profile("!test")
public class UserAuthIdentityManager {

    private final UserAuthIdentityMapper userAuthIdentityMapper;

    /**
     * 构造用户登录凭证 Manager，注入 user_auth_identities 表对应的 Mapper。
     *
     * @param userAuthIdentityMapper 登录凭证 Mapper，不能为空
     * @throws IllegalArgumentException 当 userAuthIdentityMapper 为空时
     * 业务约束：仅持有 UserAuthIdentityMapper，仅访问 user_center.user_auth_identities 单表；禁止 JOIN；禁止跨 schema；禁止跨服务数据库访问。
     */
    public UserAuthIdentityManager(UserAuthIdentityMapper userAuthIdentityMapper) {
        this.userAuthIdentityMapper = userAuthIdentityMapper;
    }

    /**
     * 根据凭证业务主键查询登录凭证记录。
     *
     * @param authId 登录凭证业务主键，不能为空
     * @return 登录凭证实体；不存在时返回 null
     * @throws IllegalArgumentException 当 authId 为空时
     * @throws DataAccessException 当数据库访问失败时
     * 业务约束：仅访问 user_center.user_auth_identities 单表；禁止 JOIN；禁止跨 schema；禁止跨服务数据库访问；不实现密码校验逻辑。
     */
    public UserAuthIdentityEntity findByAuthId(Long authId) {
        // 1. 构造单表等值查询条件
        LambdaQueryWrapper<UserAuthIdentityEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserAuthIdentityEntity::getAuthId, authId);
        // 2. 执行单表查询
        return userAuthIdentityMapper.selectOne(wrapper);
    }

    /**
     * 根据凭证类型和哈希值查询登录凭证记录。
     *
     * @param identityType 凭证类型，如 PHONE、EMAIL，不能为空
     * @param identityHash 凭证归一化哈希值，不能为空
     * @return 登录凭证实体；不存在时返回 null
     * @throws IllegalArgumentException 当 identityType 或 identityHash 为空时
     * @throws DataAccessException 当数据库访问失败时
     * 业务约束：仅访问 user_center.user_auth_identities 单表；禁止 JOIN；禁止跨 schema；禁止跨服务数据库访问；禁止在日志中输出 identityHash 以外的敏感明文。
     */
    public UserAuthIdentityEntity findByIdentityTypeAndHash(String identityType, String identityHash) {
        // 1. 构造复合等值查询条件
        LambdaQueryWrapper<UserAuthIdentityEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserAuthIdentityEntity::getIdentityType, identityType)
                .eq(UserAuthIdentityEntity::getIdentityHash, identityHash);
        // 2. 执行单表查询
        return userAuthIdentityMapper.selectOne(wrapper);
    }

    /**
     * 插入登录凭证记录。
     *
     * @param entity 待插入的登录凭证实体，不能为空
     * @return 影响行数，成功时为 1
     * @throws IllegalArgumentException 当 entity 为空时
     * @throws DataAccessException 当数据库访问失败或唯一约束冲突时
     * 业务约束：仅访问 user_center.user_auth_identities 单表；禁止 JOIN；禁止跨 schema；禁止跨服务数据库访问；不编排注册等业务流程。
     */
    public int insert(UserAuthIdentityEntity entity) {
        // 1. 执行单表插入
        return userAuthIdentityMapper.insert(entity);
    }

    /**
     * 创建登录凭证记录。
     *
     * @param authId           凭证业务主键
     * @param userId           用户业务主键
     * @param identityType     凭证类型
     * @param identityValue    脱敏后的凭证值
     * @param identityHash     凭证哈希
     * @param passwordHash     密码哈希
     * @throws IllegalArgumentException 当关键参数为空时
     * @throws DataAccessException 当数据库访问失败或唯一约束冲突时
     * 业务约束：仅访问 user_center.user_auth_identities 单表；禁止 JOIN；禁止跨 schema；禁止跨服务数据库访问。
     */
    public void createIdentity(long authId,
                               long userId,
                               String identityType,
                               String identityValue,
                               String identityHash,
                               String passwordHash) {
        if (authId <= 0 || userId <= 0) {
            throw new IllegalArgumentException("authId 或 userId 非法");
        }
        UserAuthIdentityEntity entity = new UserAuthIdentityEntity();
        entity.setAuthId(authId);
        entity.setUserId(userId);
        entity.setIdentityType(identityType);
        entity.setIdentityValue(identityValue);
        entity.setIdentityHash(identityHash);
        entity.setPasswordHash(passwordHash);
        entity.setVerified(0);
        entity.setCreatedAt(OffsetDateTime.now(ZoneOffset.UTC));
        entity.setUpdatedAt(OffsetDateTime.now(ZoneOffset.UTC));
        // 1. 执行单表插入
        userAuthIdentityMapper.insert(entity);
    }

    /**
     * 根据物理主键更新登录凭证记录。
     *
     * @param entity 待更新的登录凭证实体，不能为空且 id 不能为空
     * @return 影响行数，未命中记录时为 0
     * @throws IllegalArgumentException 当 entity 为空或 id 为空时
     * @throws DataAccessException 当数据库访问失败时
     * 业务约束：仅访问 user_center.user_auth_identities 单表；禁止 JOIN；禁止跨 schema；禁止跨服务数据库访问。
     */
    public int updateById(UserAuthIdentityEntity entity) {
        // 1. 按物理主键执行单表更新
        return userAuthIdentityMapper.updateById(entity);
    }

    /**
     * 更新登录凭证最近一次登录时间。
     *
     * @param authId       凭证业务主键
     * @param lastLoginAt  最近一次登录时间，UTC
     * @throws IllegalArgumentException 当 authId 为空或 lastLoginAt 为空时
     * @throws DataAccessException 当数据库访问失败时
     * 业务约束：仅访问 user_center.user_auth_identities 单表；禁止 JOIN；禁止跨 schema；禁止跨服务数据库访问。
     */
    public void updateLastLoginAt(Long authId, OffsetDateTime lastLoginAt) {
        if (authId == null || authId <= 0 || lastLoginAt == null) {
            throw new IllegalArgumentException("authId 或 lastLoginAt 非法");
        }
        // 1. 按 auth_id 单表更新 last_login_at
        LambdaUpdateWrapper<UserAuthIdentityEntity> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(UserAuthIdentityEntity::getAuthId, authId)
                .set(UserAuthIdentityEntity::getLastLoginAt, lastLoginAt)
                .set(UserAuthIdentityEntity::getUpdatedAt, lastLoginAt);
        userAuthIdentityMapper.update(null, wrapper);
    }
}
