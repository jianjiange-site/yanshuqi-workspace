package com.dating.im.service;

import com.dating.im.health.MinioInfraHealthChecker;
import com.dating.im.health.NacosInfraHealthChecker;
import com.dating.im.health.PostgresInfraHealthChecker;
import com.dating.im.health.RedisInfraHealthChecker;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class InfraHealthService {

    private final PostgresInfraHealthChecker postgresInfraHealthChecker;
    private final RedisInfraHealthChecker redisInfraHealthChecker;
    private final NacosInfraHealthChecker nacosInfraHealthChecker;
    private final MinioInfraHealthChecker minioInfraHealthChecker;

    public InfraHealthService(PostgresInfraHealthChecker postgresInfraHealthChecker,
                              RedisInfraHealthChecker redisInfraHealthChecker,
                              NacosInfraHealthChecker nacosInfraHealthChecker,
                              MinioInfraHealthChecker minioInfraHealthChecker) {
        this.postgresInfraHealthChecker = postgresInfraHealthChecker;
        this.redisInfraHealthChecker = redisInfraHealthChecker;
        this.nacosInfraHealthChecker = nacosInfraHealthChecker;
        this.minioInfraHealthChecker = minioInfraHealthChecker;
    }

    public Map<String, Object> checkAll() {
        Map<String, Object> checks = new LinkedHashMap<>();
        checks.put("postgresql", postgresInfraHealthChecker.check());
        checks.put("redis", redisInfraHealthChecker.check());
        checks.put("nacos", nacosInfraHealthChecker.check());
        checks.put("minio", minioInfraHealthChecker.check());

        String overall = checks.values().stream()
                .filter(Map.class::isInstance)
                .map(Map.class::cast)
                .map(item -> String.valueOf(item.get("status")))
                .allMatch(status -> "UP".equals(status) || "SKIPPED".equals(status)) ? "UP" : "DOWN";

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", overall);
        body.put("service", "im-service");
        body.put("stage", "00-B");
        body.put("checks", checks);
        return body;
    }
}