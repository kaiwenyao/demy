-- V1__Init_Course_Tables.sql
-- 课程服务初始表结构

CREATE TABLE IF NOT EXISTS courses (
    id              BIGINT          NOT NULL PRIMARY KEY,
    title           VARCHAR(255)    NOT NULL,
    description     TEXT,
    cover_image     VARCHAR(500),
    price           DECIMAL(10,2)   NOT NULL DEFAULT 0.00,
    category        VARCHAR(100),
    instructor_id   BIGINT          NOT NULL,
    level           VARCHAR(50),
    section_count   INT             NOT NULL DEFAULT 0,
    status          VARCHAR(50)     NOT NULL DEFAULT 'DRAFT',
    reject_reason   VARCHAR(500),
    valid_days      INT             COMMENT '购买后有效天数，NULL为永久',
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP
                                    ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS course_sections (
    id          BIGINT          NOT NULL PRIMARY KEY,
    course_id   BIGINT          NOT NULL,
    title       VARCHAR(255)    NOT NULL,
    duration    INT             COMMENT '时长（秒）',
    sort_order  INT             NOT NULL DEFAULT 0,
    is_free     TINYINT(1)      NOT NULL DEFAULT 0,
    created_at  DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_course_id (course_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
