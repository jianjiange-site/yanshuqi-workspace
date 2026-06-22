package com.dating.post;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * PostService 分层与关键类存在性轻量验收（不依赖 PostgreSQL / Redis / gRPC 端口）。
 */
class PostServiceArchitectureTest {

    private static final Path BASE = Path.of("src/main/java/com/dating/post");

    @Test
    void coreGrpcServiceFileShouldExist() {
        assertFileExists(BASE.resolve("grpc/PostGrpcService.java"));
    }

    @Test
    void coreServicesShouldExist() {
        List<String> services = List.of(
                "PostWriteService",
                "PostReadService",
                "PostLikeService",
                "PostCommentService",
                "FeedService");
        for (String name : services) {
            assertFileExists(BASE.resolve("service/" + name + ".java"));
        }
    }

    @Test
    void coreJobsShouldExist() {
        List<String> jobs = List.of("LikeFlushJob", "CommentFlushJob", "FeedScoreJob");
        for (String name : jobs) {
            assertFileExists(BASE.resolve("job/" + name + ".java"));
        }
    }

    @Test
    void flywayMigrationsShouldExist() {
        assertFileExists(Path.of("src/main/resources/db/migration/V001__create_post_core_tables.sql"));
        assertFileExists(Path.of("src/main/resources/db/migration/V002__create_post_interaction_tables.sql"));
    }

    @Test
    void redisKeysShouldUseYanshuqiPrefix() throws Exception {
        Path redisKeys = BASE.resolve("constant/PostRedisKeys.java");
        assertTrue(Files.exists(redisKeys));
        String content = Files.readString(redisKeys);
        assertTrue(content.contains("yanshuqi"), "PostRedisKeys 应包含 yanshuqi 前缀");
    }

    private void assertFileExists(Path path) {
        assertTrue(Files.exists(path), "缺少关键文件: " + path);
    }
}
