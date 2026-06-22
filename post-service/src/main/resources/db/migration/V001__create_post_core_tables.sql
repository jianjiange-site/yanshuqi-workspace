-- POST-02：创建 post_center 核心业务表（帖子主表、图片、计数底座）

CREATE SCHEMA IF NOT EXISTS post_center;

COMMENT ON SCHEMA post_center IS 'post-service 内容域 schema（yanshuqi）';

-- A. posts 帖子主表
CREATE TABLE post_center.posts (
    id          BIGSERIAL PRIMARY KEY,
    post_id     BIGINT        NOT NULL,
    user_id     BIGINT        NOT NULL,
    content     VARCHAR(1024) NOT NULL,
    status      SMALLINT      NOT NULL DEFAULT 1,
    deleted     SMALLINT      NOT NULL DEFAULT 0,
    created_at  TIMESTAMPTZ   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMPTZ   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_posts_post_id UNIQUE (post_id)
);

COMMENT ON TABLE post_center.posts IS '帖子主表：存储发帖内容与作者，对外 postId 映射 post_id 字段';
COMMENT ON COLUMN post_center.posts.id IS '数据库自增主键，仅用于物理存储，不对外暴露';
COMMENT ON COLUMN post_center.posts.post_id IS '帖子业务主键，对外 postId 映射该字段';
COMMENT ON COLUMN post_center.posts.user_id IS '发帖用户业务 ID';
COMMENT ON COLUMN post_center.posts.content IS '帖子文本内容，最长 1024 字符';
COMMENT ON COLUMN post_center.posts.status IS '帖子状态：1=正常展示，0=已下线/删除';
COMMENT ON COLUMN post_center.posts.deleted IS '逻辑删除：0=未删除，1=已删除';
COMMENT ON COLUMN post_center.posts.created_at IS '创建时间，UTC';
COMMENT ON COLUMN post_center.posts.updated_at IS '更新时间，UTC';

CREATE INDEX idx_posts_user_created
    ON post_center.posts (user_id, created_at DESC);

CREATE INDEX idx_posts_post_id_active
    ON post_center.posts (post_id)
    WHERE deleted = 0;

-- B. post_images 帖子图片表
CREATE TABLE post_center.post_images (
    post_id     BIGINT        NOT NULL,
    sort_order  SMALLINT      NOT NULL,
    image_key   VARCHAR(256)  NOT NULL,
    created_at  TIMESTAMPTZ   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (post_id, sort_order)
);

COMMENT ON TABLE post_center.post_images IS '帖子图片表：仅存 MinIO object key，最多 9 张，按 sort_order 排序';
COMMENT ON COLUMN post_center.post_images.post_id IS '所属帖子业务 ID';
COMMENT ON COLUMN post_center.post_images.sort_order IS '图片排序序号，从 0 开始';
COMMENT ON COLUMN post_center.post_images.image_key IS 'MinIO object key，不存 URL';
COMMENT ON COLUMN post_center.post_images.created_at IS '创建时间，UTC';

CREATE INDEX idx_post_images_post_id
    ON post_center.post_images (post_id);

-- C. post_stats 帖子计数底座
CREATE TABLE post_center.post_stats (
    post_id        BIGINT      PRIMARY KEY,
    like_count     INT         NOT NULL DEFAULT 0,
    comment_count  INT         NOT NULL DEFAULT 0,
    updated_at     TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE post_center.post_stats IS '帖子计数底座：点赞/评论 DB 基准值，阶段 3 配合 Redis 增量刷盘';
COMMENT ON COLUMN post_center.post_stats.post_id IS '所属帖子业务 ID';
COMMENT ON COLUMN post_center.post_stats.like_count IS '点赞数 DB 基准值';
COMMENT ON COLUMN post_center.post_stats.comment_count IS '评论数 DB 基准值';
COMMENT ON COLUMN post_center.post_stats.updated_at IS '计数更新时间，UTC';
