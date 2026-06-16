package com.dating.user.service.support;

import org.springframework.stereotype.Component;

/**
 * 业务主键生成器，统一生成 user_id、auth_id 等业务主键。
 */
@Component
public class BusinessIdGenerator {

    private final SnowflakeIdGenerator snowflakeIdGenerator;

    /**
     * 构造业务主键生成器。
     */
    public BusinessIdGenerator() {
        this.snowflakeIdGenerator = new SnowflakeIdGenerator(1, 1);
    }

    /**
     * 生成下一个业务主键。
     *
     * @return 业务主键
     */
    public long nextId() {
        return snowflakeIdGenerator.nextId();
    }
}
