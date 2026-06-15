package com.dating.post.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
public class HealthController {

    private static final String SERVICE_NAME = "post-service";

    @GetMapping("/health")
    public Map<String, Object> health() {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", "UP");
        body.put("service", SERVICE_NAME);
        body.put("timestamp", Instant.now().toString());
        body.put("stage", "00-B");
        return body;
    }
}
