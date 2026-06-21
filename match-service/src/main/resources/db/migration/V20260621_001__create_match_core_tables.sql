-- MATCH-02：创建 match_center schema 及 4 张 Match 核心业务表

CREATE SCHEMA IF NOT EXISTS match_center;

COMMENT ON SCHEMA match_center IS 'match-service 匹配域 schema（yanshuqi）';

-- A. user_swipe_history 划卡历史
CREATE TABLE match_center.user_swipe_history (
    id               BIGSERIAL PRIMARY KEY,
    biz_id           BIGINT       NOT NULL,
    user_id          BIGINT       NOT NULL,
    target_user_id   BIGINT       NOT NULL,
    target_user_type SMALLINT     NOT NULL,
    direction        SMALLINT     NOT NULL,
    swiped_at        TIMESTAMPTZ  NOT NULL,
    created_at       TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at       TIMESTAMPTZ  NOT NULL DEFAULT now(),
    deleted          INTEGER      NOT NULL DEFAULT 0,
    CONSTRAINT uk_user_swipe_biz_id UNIQUE (biz_id),
    CONSTRAINT uk_user_swipe_user_target UNIQUE (user_id, target_user_id)
);

COMMENT ON TABLE match_center.user_swipe_history IS '划卡历史：召回过滤与 Swipe 幂等的权威来源';
COMMENT ON COLUMN match_center.user_swipe_history.id IS '数据库自增主键，仅用于物理存储，不对外暴露';
COMMENT ON COLUMN match_center.user_swipe_history.biz_id IS '划卡记录业务主键，对外引用使用该字段';
COMMENT ON COLUMN match_center.user_swipe_history.user_id IS '发起划卡用户业务 ID';
COMMENT ON COLUMN match_center.user_swipe_history.target_user_id IS '目标用户业务 ID';
COMMENT ON COLUMN match_center.user_swipe_history.target_user_type IS '目标用户类型：1=BH，2=DH';
COMMENT ON COLUMN match_center.user_swipe_history.direction IS '划卡方向：1=LEFT，2=RIGHT，3=SUPER_HI';
COMMENT ON COLUMN match_center.user_swipe_history.swiped_at IS '划卡时间，UTC';
COMMENT ON COLUMN match_center.user_swipe_history.deleted IS '逻辑删除：0=未删除，1=已删除';
COMMENT ON CONSTRAINT uk_user_swipe_user_target ON match_center.user_swipe_history IS
    '同一用户对同一 target 的 swipe 幂等；任意方向 LEFT/RIGHT/SUPER_HI 都算看过，后续召回需排除';

CREATE INDEX idx_user_swipe_history_user_time
    ON match_center.user_swipe_history (user_id, swiped_at DESC);

CREATE INDEX idx_user_swipe_history_target_dir
    ON match_center.user_swipe_history (target_user_id, direction);

-- B. match 匹配关系（PostgreSQL 保留字，需双引号）
CREATE TABLE match_center."match" (
    id            BIGSERIAL PRIMARY KEY,
    biz_id        BIGINT       NOT NULL,
    user_id_low   BIGINT       NOT NULL,
    user_id_high  BIGINT       NOT NULL,
    matched_at    TIMESTAMPTZ  NOT NULL,
    source        VARCHAR(30)  NOT NULL,
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at    TIMESTAMPTZ  NOT NULL DEFAULT now(),
    deleted       INTEGER      NOT NULL DEFAULT 0,
    CONSTRAINT uk_match_biz_id UNIQUE (biz_id),
    CONSTRAINT uk_match_pair UNIQUE (user_id_low, user_id_high)
);

COMMENT ON TABLE match_center."match" IS '匹配关系：user_id_low=min(uid1,uid2)，user_id_high=max(uid1,uid2)，避免 A-B/B-A 重复';
COMMENT ON COLUMN match_center."match".id IS '数据库自增主键，仅用于物理存储，不对外暴露';
COMMENT ON COLUMN match_center."match".biz_id IS '匹配业务主键，对外 matchId 映射该字段';
COMMENT ON COLUMN match_center."match".user_id_low IS '用户对较小 user_id';
COMMENT ON COLUMN match_center."match".user_id_high IS '用户对较大 user_id';
COMMENT ON COLUMN match_center."match".source IS '匹配来源：SWIPE_MATCH、SWIPE_SUPER_HI';
COMMENT ON COLUMN match_center."match".deleted IS '逻辑删除：0=未删除，1=已删除';

CREATE INDEX idx_match_low_time
    ON match_center."match" (user_id_low, matched_at DESC);

CREATE INDEX idx_match_high_time
    ON match_center."match" (user_id_high, matched_at DESC);

-- C. match_outbox 匹配副作用 outbox
CREATE TABLE match_center.match_outbox (
    id            BIGSERIAL PRIMARY KEY,
    biz_id        BIGINT       NOT NULL,
    match_biz_id  BIGINT       NOT NULL,
    action        VARCHAR(40)  NOT NULL,
    payload_json  JSONB        NOT NULL,
    attempts      INTEGER      NOT NULL DEFAULT 0,
    next_retry_at TIMESTAMPTZ  NOT NULL,
    status        VARCHAR(20)  NOT NULL,
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at    TIMESTAMPTZ  NOT NULL DEFAULT now(),
    deleted       INTEGER      NOT NULL DEFAULT 0,
    CONSTRAINT uk_match_outbox_biz_id UNIQUE (biz_id)
);

COMMENT ON TABLE match_center.match_outbox IS '匹配成功外部副作用 outbox：解决本地 match 已创建但 im-service 调用失败的一致性问题';
COMMENT ON COLUMN match_center.match_outbox.id IS '数据库自增主键，仅用于物理存储，不对外暴露';
COMMENT ON COLUMN match_center.match_outbox.biz_id IS 'outbox 事件业务主键';
COMMENT ON COLUMN match_center.match_outbox.match_biz_id IS '关联 match.biz_id，不使用 match.id';
COMMENT ON COLUMN match_center.match_outbox.status IS '状态：PENDING、DONE、DEAD';
COMMENT ON COLUMN match_center.match_outbox.deleted IS '逻辑删除：0=未删除，1=已删除';

CREATE INDEX idx_match_outbox_status_retry
    ON match_center.match_outbox (status, next_retry_at);

CREATE INDEX idx_match_outbox_match_biz_id
    ON match_center.match_outbox (match_biz_id);

-- D. profile_visit 主页访问
CREATE TABLE match_center.profile_visit (
    id               BIGSERIAL PRIMARY KEY,
    biz_id           BIGINT       NOT NULL,
    from_user_id     BIGINT       NOT NULL,
    target_user_id   BIGINT       NOT NULL,
    visit_count      INTEGER      NOT NULL DEFAULT 1,
    first_visited_at TIMESTAMPTZ  NOT NULL,
    last_visited_at  TIMESTAMPTZ  NOT NULL,
    created_at       TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at       TIMESTAMPTZ  NOT NULL DEFAULT now(),
    deleted          INTEGER      NOT NULL DEFAULT 0,
    CONSTRAINT uk_profile_visit_biz_id UNIQUE (biz_id),
    CONSTRAINT uk_profile_visit_pair UNIQUE (from_user_id, target_user_id)
);

COMMENT ON TABLE match_center.profile_visit IS '主页访问记录：重复访问 UPSERT 累加 visit_count';
COMMENT ON COLUMN match_center.profile_visit.id IS '数据库自增主键，仅用于物理存储，不对外暴露';
COMMENT ON COLUMN match_center.profile_visit.biz_id IS '访问记录业务主键，对外 visitId 映射该字段';
COMMENT ON COLUMN match_center.profile_visit.deleted IS '逻辑删除：0=未删除，1=已删除';
COMMENT ON CONSTRAINT uk_profile_visit_pair ON match_center.profile_visit IS
    'from_user_id + target_user_id 唯一，重复访问不新增行，只累加 visit_count';

CREATE INDEX idx_profile_visit_target_time
    ON match_center.profile_visit (target_user_id, last_visited_at DESC);

CREATE INDEX idx_profile_visit_from_time
    ON match_center.profile_visit (from_user_id, last_visited_at DESC);
