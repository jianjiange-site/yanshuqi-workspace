package com.dating.user.service.storage;

import com.dating.user.config.ObjectStorageProperties;
import com.dating.user.exception.UserBizException;
import com.dating.user.exception.UserErrorCode;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Mock 对象存储：dev/test 可重复验证 presign/confirm，无需真实 MinIO 上传。
 */
@Service
@ConditionalOnProperty(name = "object.storage.mode", havingValue = "mock", matchIfMissing = true)
public class MockObjectStorageService implements ObjectStorageService {

    private final ObjectStorageProperties objectStorageProperties;

    /** presign 时预注册对象，confirm statObject 时可命中。 */
    private final ConcurrentMap<String, StoredObject> registry = new ConcurrentHashMap<>();

    public MockObjectStorageService(ObjectStorageProperties objectStorageProperties) {
        this.objectStorageProperties = objectStorageProperties;
    }

    @Override
    public PresignPutResult presignPutObject(String objectKey,
                                             String contentType,
                                             int expireSeconds,
                                             long expectedSizeBytes) {
        registry.put(objectKey, new StoredObject(expectedSizeBytes, contentType, 0, 0));
        long expiresAtMs = System.currentTimeMillis() + expireSeconds * 1000L;
        String base = objectStorageProperties.getPublicBaseUrl();
        if (base == null || base.isBlank()) {
            base = "mock://object-storage";
        }
        String presignedUrl = base.replaceAll("/$", "") + "/put/"
                + objectStorageProperties.getBucket() + "/" + objectKey
                + "?expires=" + expiresAtMs;
        return new PresignPutResult(presignedUrl, expiresAtMs);
    }

    @Override
    public ObjectStat statObject(String objectKey) {
        StoredObject stored = registry.get(objectKey);
        if (stored == null) {
            throw new UserBizException(UserErrorCode.AVATAR_OBJECT_NOT_FOUND);
        }
        return new ObjectStat(stored.sizeBytes, stored.contentType, stored.width, stored.height);
    }

    /**
     * 测试或冒烟脚本可手动注册对象。
     */
    public void registerForTest(String objectKey, long sizeBytes, String contentType, int width, int height) {
        registry.put(objectKey, new StoredObject(sizeBytes, contentType, width, height));
    }

    private record StoredObject(long sizeBytes, String contentType, int width, int height) {
    }
}
