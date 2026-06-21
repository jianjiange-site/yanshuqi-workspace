package com.dating.user.service.storage;

/**
 * 对象存储抽象，业务层不依赖 MinIO/S3 SDK 细节。
 */
public interface ObjectStorageService {

    /**
     * 签发 PUT presigned URL。
     *
     * @param objectKey           对象 key
     * @param contentType         Content-Type
     * @param expireSeconds       过期秒数
     * @param expectedSizeBytes   客户端声明大小（mock 模式用于预注册）
     * @return presign 结果
     */
    PresignPutResult presignPutObject(String objectKey,
                                      String contentType,
                                      int expireSeconds,
                                      long expectedSizeBytes);

    /**
     * 查询对象元信息，确认文件已上传。
     *
     * @param objectKey 对象 key
     * @return 对象 stat 信息
     */
    ObjectStat statObject(String objectKey);
}
