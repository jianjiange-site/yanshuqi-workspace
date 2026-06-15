package com.dating.gateway.health;

import com.dating.gateway.config.MinioProperties;
import io.minio.MinioClient;
import io.minio.BucketExistsArgs;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class MinioInfraHealthChecker {

    private final MinioClient minioClient;
    private final MinioProperties minioProperties;

    public MinioInfraHealthChecker(MinioClient minioClient, MinioProperties minioProperties) {
        this.minioClient = minioClient;
        this.minioProperties = minioProperties;
    }

    public Map<String, Object> check() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("component", "minio");
        result.put("bucket", minioProperties.getBucket());
        result.put("pathStyleAccess", minioProperties.isPathStyleAccess());
        try {
            boolean exists = minioClient.bucketExists(
                    BucketExistsArgs.builder().bucket(minioProperties.getBucket()).build());
            result.put("bucketExists", exists);
            result.put("status", exists ? "UP" : "DOWN");
            if (!exists) {
                result.put("error", "Bucket not found: " + minioProperties.getBucket());
            }
        } catch (Exception ex) {
            result.put("status", "DOWN");
            result.put("error", ex.getClass().getSimpleName() + ": " + ex.getMessage());
        }
        return result;
    }
}