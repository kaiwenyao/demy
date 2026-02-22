-- 订单表
CREATE TABLE orders (
    id              BIGINT          NOT NULL PRIMARY KEY COMMENT '订单ID',
    user_id         BIGINT          NOT NULL COMMENT '用户ID',
    course_id       BIGINT          NOT NULL COMMENT '课程ID',
    amount          DECIMAL(10,2)   NOT NULL COMMENT '实付金额',
    status          VARCHAR(20)     NOT NULL DEFAULT 'PENDING'
                                    COMMENT 'PENDING/PAID/CANCELLED',
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP
                                    ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY idx_user_course (user_id, course_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单表';
