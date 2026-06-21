package com.dating.user.service.storage;

import com.dating.user.config.MinioProperties;
import com.dating.user.config.ObjectStorageProperties;
import com.dating.user.exception.UserBizException;
import com.dating.user.exception.UserErrorCode;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.StatObjectArgs;
import io.minio.StatObjectResponse;
import io.minio.http.Method;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

/**
 * MinIO 对象存储实现，生产环境替换 mock 实现。
 */
@Service
@ConditionalOnProperty(name = "object.storage.mode", havingValue = "minio")
public class MinioObjectStorageService implements ObjectStorageService {

    private static final Logger log = LoggerFactory.getLogger(MinioObjectStorageService.class);

    private final MinioClient minioClient;

    private final MinioProperties minioProperties;

    private final ObjectStorageProperties objectStorageProperties;

    public MinioObjectStorageService(MinioClient minioClient,
                                     MinioProperties minioProperties,
                                     ObjectStorageProperties objectStorageProperties) {
        this.minioClient = minioClient;
        this.minioProperties = minioProperties;
        this.objectStorageProperties = objectStorageProperties;
    }

    @Override
    public PresignPutResult presignPutObject(String objectKey,
                                             String contentType,
                                             int expireSeconds,
                                             long expectedSizeBytes) {
        try {
            String url = minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.PUT)
                            .bucket(resolveBucket())
                            .object(objectKey)
                            .expiry(expireSeconds)
                            .build());
            long expiresAtMs = System.currentTimeMillis() + expireSeconds * 1000L;
            return new PresignPutResult(url, expiresAtMs);
        } catch (Exception ex) {
            log.warn("MinIO presign 失败, objectKey={}", objectKey, ex);
            throw new UserBizException(UserErrorCode.AVATAR_PRESIGN_FAILED);
        }
    }

    @Override
    public ObjectStat statObject(String objectKey) {
        try {
            StatObjectResponse stat = minioClient.statObject(
                    StatObjectArgs.builder()
                            .bucket(resolveBucket())
                            .object(objectKey)
                            .build());
            return new ObjectStat(stat.size(), stat.contentType(), 0, 0);
        } catch (UserBizException ex) {
            throw ex;
        } catch (Exception ex) {
            String message = ex.getMessage() == null ? "" : ex.getMessage().toLowerCase();
            if (message.contains("not found") || message.contains("nosuchkey")) {
                throw new UserBizException(UserErrorCode.AVATAR_OBJECT_NOT_FOUND);
            }
            log.warn("MinIO statObject 失败, objectKey={}", objectKey, ex);
            throw new UserBizException(UserErrorCode.AVATAR_OBJECT_STAT_FAILED);
        }
    }

    private String resolveBucket() {
        String configured = objectStorageProperties.getBucket();
        if (configured != null && !configured.isBlank()) {
            return configured;
        }
        return minioProperties.getBucket();
    }
}
