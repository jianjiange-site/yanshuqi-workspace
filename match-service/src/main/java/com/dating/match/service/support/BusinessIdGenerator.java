package com.dating.match.service.support;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * 业务主键生成器，生成 biz_id 等雪花 ID。
 */
@Component
@Profile("!test")
public class BusinessIdGenerator {

    private final SnowflakeIdGenerator snowflakeIdGenerator;

    public BusinessIdGenerator() {
        this.snowflakeIdGenerator = new SnowflakeIdGenerator(2, 1);
    }

    public long nextId() {
        return snowflakeIdGenerator.nextId();
    }
}
