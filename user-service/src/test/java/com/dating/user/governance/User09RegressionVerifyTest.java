package com.dating.user.governance;

import com.dating.user.constant.RedisKeyConstants;
import com.dating.user.exception.UserErrorCode;
import com.dating.user.exception.UserGrpcStatusMapper;
import io.grpc.Status;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * USER-09 治理回归：静态检查 Redis key、错误码映射、禁止 FLUSH 命令。
 */
class User09RegressionVerifyTest {

    @Test
    void redisKeyConstantsShouldNotIncludeHomeCardCombo() {
        assertFalse(RedisKeyConstants.allUserProfileCacheKeys(1L).stream()
                .anyMatch(k -> k.contains("home_card")));
    }

    @Test
    void everyUserErrorCodeShouldHaveGrpcMapping() {
        for (UserErrorCode code : UserErrorCode.values()) {
            Status status = UserGrpcStatusMapper.toStatus(code);
            assertNotNull(status);
            assertNotNull(status.getCode());
        }
    }

    @Test
    void sourceShouldNotContainFlushDbOrFlushAll() throws Exception {
        Path src = resolveSrcRoot();
        try (Stream<Path> paths = Files.walk(src)) {
            boolean hasFlush = paths
                    .filter(p -> p.toString().endsWith(".java"))
                    .flatMap(p -> {
                        try {
                            return Files.readAllLines(p).stream();
                        } catch (Exception e) {
                            return Stream.empty();
                        }
                    })
                    .anyMatch(line -> {
                        String trim = line.trim();
                        if (trim.startsWith("*") || trim.startsWith("//")) {
                            return false;
                        }
                        if (trim.contains("contains(\"FLUSHDB\")") || trim.contains("contains(\"FLUSHALL\")")) {
                            return false;
                        }
                        return line.contains("FLUSHDB") || line.contains("FLUSHALL");
                    });
            assertFalse(hasFlush, "源码中不应出现 FLUSHDB/FLUSHALL");
        }
    }

    @Test
    void defaultProfileTtlShouldBeSixHundredSeconds() {
        assertEquals(600, RedisKeyConstants.DEFAULT_PROFILE_TTL_SECONDS);
    }

    private Path resolveSrcRoot() {
        Path cwd = Paths.get("").toAbsolutePath();
        Path direct = cwd.resolve("src/main/java");
        if (direct.toFile().exists()) {
            return direct;
        }
        Path nested = cwd.resolve("user-service/src/main/java");
        if (nested.toFile().exists()) {
            return nested;
        }
        return direct;
    }
}
