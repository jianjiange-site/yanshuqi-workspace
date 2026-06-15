package com.dating.im.controller;

import com.dating.im.service.InfraHealthService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
public class InfraHealthController {

    private final InfraHealthService infraHealthService;

    public InfraHealthController(InfraHealthService infraHealthService) {
        this.infraHealthService = infraHealthService;
    }

    @GetMapping("/health/infra")
    public Map<String, Object> infraHealth() {
        Map<String, Object> body = new LinkedHashMap<>(infraHealthService.checkAll());
        body.put("timestamp", Instant.now().toString());
        return body;
    }
}