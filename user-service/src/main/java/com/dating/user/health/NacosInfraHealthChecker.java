package com.dating.user.health;

import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.naming.NamingService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

@Component
public class NacosInfraHealthChecker {

    @Value("${spring.cloud.nacos.discovery.server-addr}")
    private String serverAddr;

    @Value("${spring.cloud.nacos.discovery.namespace:yanshuqi-dev}")
    private String namespace;

    @Value("${spring.cloud.nacos.discovery.group:DEFAULT_GROUP}")
    private String group;

    @Value("${spring.application.name}")
    private String applicationName;

    @Value("${spring.cloud.nacos.discovery.username:}")
    private String username;

    @Value("${spring.cloud.nacos.discovery.password:}")
    private String password;

    public Map<String, Object> check() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("component", "nacos");
        result.put("namespace", namespace);
        result.put("group", group);
        result.put("dataId", applicationName + "-dev.yaml");
        try {
            Properties properties = new Properties();
            properties.put("serverAddr", serverAddr);
            properties.put("namespace", namespace);
            properties.put("username", username);
            properties.put("password", password);

            ConfigService configService = NacosFactory.createConfigService(properties);
            NamingService namingService = NacosFactory.createNamingService(properties);
            String configStatus = configService.getServerStatus();
            String namingStatus = namingService.getServerStatus();

            result.put("configStatus", configStatus);
            result.put("discoveryStatus", namingStatus);
            boolean up = "UP".equalsIgnoreCase(configStatus) && "UP".equalsIgnoreCase(namingStatus);
            result.put("status", up ? "UP" : "DOWN");
            if (!up) {
                result.put("error", "Nacos server not UP");
            }
        } catch (Exception ex) {
            result.put("status", "DOWN");
            result.put("error", ex.getClass().getSimpleName() + ": " + ex.getMessage());
        }
        return result;
    }
}