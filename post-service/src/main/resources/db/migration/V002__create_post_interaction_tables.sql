-- POST-03：创建点赞与评论互动表

-- A. post_likes 点赞幂等表
CREATE TABLE post_center.post_likes (
    user_id    BIGINT       NOT NULL,
    post_id    BIGINT       NOT NULL,
    status     SMALLINT     NOT NULL DEFAULT 1,
    created_at TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id, post_id)
);

COMMENT ON TABLE post_center.post_likes IS '点赞幂等表：联合主键 (user_id, post_id) 保证同一用户对同一帖子只一条记录';
COMMENT ON COLUMN post_center.post_likes.user_id IS '点赞用户业务 ID';
COMMENT ON COLUMN post_center.post_likes.post_id IS '被点赞帖子业务 ID';
COMMENT ON COLUMN post_center.post_likes.status IS '点赞状态：1=LIKE，0=UNLIKE';

CREATE INDEX idx_post_likes_post_id
    ON post_center.post_likes (post_id);

-- B. post_comments 评论表
CREATE TABLE post_center.post_comments (
    id                BIGSERIAL    PRIMARY KEY,
    comment_id        BIGINT       NOT NULL,
    post_id           BIGINT       NOT NULL,
    user_id           BIGINT       NOT NULL,
    root_id           BIGINT       NOT NULL DEFAULT 0,
    parent_id         BIGINT       NOT NULL DEFAULT 0,
    reply_to_user_id  BIGINT       NOT NULL DEFAULT 0,
    content           VARCHAR(512) NOT NULL,
    status            SMALLINT     NOT NULL DEFAULT 1,
    deleted           SMALLINT     NOT NULL DEFAULT 0,
    created_at        TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at        TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_post_comments_comment_id UNIQUE (comment_id)
);

COMMENT ON TABLE post_center.post_comments IS '评论表：当前仅一级评论，预留 root_id/parent_id 供后续楼中楼扩展';
COMMENT ON COLUMN post_center.post_comments.comment_id IS '评论业务主键，对外 commentId 映射该字段';
COMMENT ON COLUMN post_center.post_comments.post_id IS '所属帖子业务 ID';
COMMENT ON COLUMN post_center.post_comments.user_id IS '评论作者业务 ID';
COMMENT ON COLUMN post_center.post_comments.root_id IS '根评论 ID，一级评论为 0';
COMMENT ON COLUMN post_center.post_comments.parent_id IS '父评论 ID，一级评论为 0';
COMMENT ON COLUMN post_center.post_comments.reply_to_user_id IS '被回复用户 ID，一级评论为 0';
COMMENT ON COLUMN post_center.post_comments.content IS '评论内容，最长 512 字符';
COMMENT ON COLUMN post_center.post_comments.status IS '评论状态：1=正常，0=已删除';
COMMENT ON COLUMN post_center.post_comments.deleted IS '逻辑删除：0=未删除，1=已删除';

CREATE INDEX idx_post_comments_post_root
    ON post_center.post_comments (post_id, root_id, comment_id DESC);
