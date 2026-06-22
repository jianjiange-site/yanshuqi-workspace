package com.dating.post.service.support;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * 业务主键生成器，生成 postId 等雪花 ID。
 */
@Component
@Profile("!test")
public class BusinessIdGenerator {

    private final SnowflakeIdGenerator snowflakeIdGenerator;

    public BusinessIdGenerator() {
        // post-service 使用 workerId=3，与 user/match 区分；后续可改为配置项。
        this.snowflakeIdGenerator = new SnowflakeIdGenerator(3, 1);
    }

    public long nextId() {
        return snowflakeIdGenerator.nextId();
    }
}
