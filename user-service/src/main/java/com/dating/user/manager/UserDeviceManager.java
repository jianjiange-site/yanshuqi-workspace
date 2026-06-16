package com.dating.user.manager;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.dating.user.entity.UserDeviceEntity;
import com.dating.user.mapper.UserDeviceMapper;
import org.springframework.context.annotation.Profile;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;

/**
 * 用户设备表数据访问编排，仅封装 user_center.user_devices 单表操作。
 */
@Component
@Profile("!test")
public class UserDeviceManager {

    private final UserDeviceMapper userDeviceMapper;

    /**
     * 构造用户设备 Manager，注入 user_devices 表对应的 Mapper。
     *
     * @param userDeviceMapper 用户设备 Mapper，不能为空
     * @throws IllegalArgumentException 当 userDeviceMapper 为空时
     * 业务约束：仅持有 UserDeviceMapper，仅访问 user_center.user_devices 单表；禁止 JOIN；禁止跨 schema；禁止跨服务数据库访问。
     */
    public UserDeviceManager(UserDeviceMapper userDeviceMapper) {
        this.userDeviceMapper = userDeviceMapper;
    }

    /**
     * 根据设备业务主键查询用户设备记录。
     *
     * @param deviceId 设备业务主键，不能为空
     * @return 用户设备实体；不存在时返回 null
     * @throws IllegalArgumentException 当 deviceId 为空时
     * @throws DataAccessException 当数据库访问失败时
     * 业务约束：仅访问 user_center.user_devices 单表；禁止 JOIN；禁止跨 schema；禁止跨服务数据库访问。
     */
    public UserDeviceEntity findByDeviceId(Long deviceId) {
        // 1. 构造单表等值查询条件
        LambdaQueryWrapper<UserDeviceEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserDeviceEntity::getDeviceId, deviceId);
        // 2. 执行单表查询
        return userDeviceMapper.selectOne(wrapper);
    }

    /**
     * 根据用户业务主键和设备指纹查询用户设备记录。
     *
     * @param userId 用户业务主键，不能为空
     * @param deviceFingerprint 设备指纹，不能为空
     * @return 用户设备实体；不存在时返回 null
     * @throws IllegalArgumentException 当 userId 或 deviceFingerprint 为空时
     * @throws DataAccessException 当数据库访问失败时
     * 业务约束：仅访问 user_center.user_devices 单表；禁止 JOIN；禁止跨 schema；禁止跨服务数据库访问；不实现设备风控逻辑。
     */
    public UserDeviceEntity findByUserIdAndFingerprint(Long userId, String deviceFingerprint) {
        // 1. 构造单表复合等值查询条件
        LambdaQueryWrapper<UserDeviceEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserDeviceEntity::getUserId, userId)
                .eq(UserDeviceEntity::getDeviceFingerprint, deviceFingerprint);
        // 2. 执行单表查询
        return userDeviceMapper.selectOne(wrapper);
    }

    /**
     * 插入用户设备记录。
     *
     * @param entity 待插入的用户设备实体，不能为空
     * @return 影响行数，成功时为 1
     * @throws IllegalArgumentException 当 entity 为空时
     * @throws DataAccessException 当数据库访问失败或唯一约束冲突时
     * 业务约束：仅访问 user_center.user_devices 单表；禁止 JOIN；禁止跨 schema；禁止跨服务数据库访问；不编排登录设备 upsert 等业务流程。
     */
    public int insert(UserDeviceEntity entity) {
        // 1. 执行单表插入
        return userDeviceMapper.insert(entity);
    }

    /**
     * 根据物理主键更新用户设备记录。
     *
     * @param entity 待更新的用户设备实体，不能为空且 id 不能为空
     * @return 影响行数，未命中记录时为 0
     * @throws IllegalArgumentException 当 entity 为空或 id 为空时
     * @throws DataAccessException 当数据库访问失败时
     * 业务约束：仅访问 user_center.user_devices 单表；禁止 JOIN；禁止跨 schema；禁止跨服务数据库访问。
     */
    public int updateById(UserDeviceEntity entity) {
        // 1. 按物理主键执行单表更新
        return userDeviceMapper.updateById(entity);
    }
}
