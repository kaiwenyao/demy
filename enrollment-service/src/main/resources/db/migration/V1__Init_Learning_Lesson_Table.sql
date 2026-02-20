-- V1__Init_Learning_Lesson_Table.sql
-- Initial migration for enrollment-service enrollments table.

CREATE TABLE IF NOT EXISTS enrollments (
    id BIGINT NOT NULL COMMENT '主键',
    user_id BIGINT NOT NULL COMMENT '学员id',
    course_id BIGINT NOT NULL COMMENT '课程id',
    status TINYINT DEFAULT '0' COMMENT '课程状态，0-未学习，1-学习中，2-已学完，3-已失效',
    week_freq TINYINT DEFAULT NULL COMMENT '每周学习频率，例如每周学习6小节，则频率为6',
    plan_status TINYINT NOT NULL DEFAULT '0' COMMENT '学习计划状态，0-没有计划，1-计划进行中',
    learned_sections INT NOT NULL DEFAULT '0' COMMENT '已学习小节数量',
    latest_section_id BIGINT DEFAULT NULL COMMENT '最近一次学习的小节id',
    latest_learn_time DATETIME DEFAULT NULL COMMENT '最近一次学习的时间',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    expire_time DATETIME DEFAULT NULL COMMENT '过期时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id) USING BTREE,
    UNIQUE KEY idx_user_id (user_id, course_id) USING BTREE
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_0900_ai_ci
  ROW_FORMAT=DYNAMIC
  COMMENT='学生课程表';
