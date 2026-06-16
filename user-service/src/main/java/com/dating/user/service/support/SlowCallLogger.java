package com.dating.user.service.support;

import com.dating.user.config.UserCacheProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * 慢调用日志记录器，覆盖关键业务路径。
 */
@Component
public class SlowCallLogger {

    private static final Logger log = LoggerFactory.getLogger(SlowCallLogger.class);

    private final int slowCallThresholdMs;

    /**
     * 构造慢调用日志记录器。
     *
     * @param userCacheProperties 用户缓存配置
     */
    public SlowCallLogger(UserCacheProperties userCacheProperties) {
        this.slowCallThresholdMs = userCacheProperties.getSlowCallThresholdMs();
    }

    /**
     * 供单元测试创建默认阈值的慢调用记录器。
     *
     * @return 慢调用记录器
     */
    public static SlowCallLogger forTest() {
        return new SlowCallLogger();
    }

    /**
     * 默认阈值构造，仅供测试使用。
     */
    public SlowCallLogger() {
        this.slowCallThresholdMs = 500;
    }

    /**
     * 记录单用户关键路径慢调用。
     *
     * @param method    方法名
     * @param startNano 开始时间纳秒
     * @param userId    用户 ID，可为 null
     * @param success   是否成功
     * @param errorCode 错误码，可为 null
     */
    public void logIfSlow(String method, long startNano, Long userId, boolean success, String errorCode) {
        long costMs = toMillis(startNano);
        if (costMs < slowCallThresholdMs) {
            return;
        }
        if (userId != null) {
            log.warn("慢调用, method={}, costMs={}, success={}, errorCode={}, userId={}",
                    method, costMs, success, errorCode, userId);
            return;
        }
        log.warn("慢调用, method={}, costMs={}, success={}, errorCode={}",
                method, costMs, success, errorCode);
    }

    /**
     * 记录批量查询慢调用，仅打印 userIds 数量。
     *
     * @param method       方法名
     * @param startNano    开始时间纳秒
     * @param userIdsSize  用户 ID 数量
     * @param cacheHit     缓存命中数
     * @param cacheMiss    缓存未命中数
     * @param success      是否成功
     * @param errorCode    错误码，可为 null
     */
    public void logBatchIfSlow(String method,
                               long startNano,
                               int userIdsSize,
                               int cacheHit,
                               int cacheMiss,
                               boolean success,
                               String errorCode) {
        long costMs = toMillis(startNano);
        if (costMs < slowCallThresholdMs) {
            return;
        }
        log.warn("慢调用, method={}, costMs={}, success={}, errorCode={}, userIdsSize={}, cacheHit={}, cacheMiss={}",
                method, costMs, success, errorCode, userIdsSize, cacheHit, cacheMiss);
    }

    private long toMillis(long startNano) {
        return (System.nanoTime() - startNano) / 1_000_000L;
    }
}
