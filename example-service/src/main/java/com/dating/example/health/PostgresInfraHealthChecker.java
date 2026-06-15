package com.dating.example.health;

import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class PostgresInfraHealthChecker {

    public Map<String, Object> check() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("component", "postgresql");
        result.put("status", "SKIPPED");
        result.put("reason", "No dedicated schema for example-service in Stage 00-B");
        return result;
    }
}