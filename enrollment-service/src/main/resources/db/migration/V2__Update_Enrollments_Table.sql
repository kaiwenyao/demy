-- V2__Update_Enrollments_Table.sql
-- 更新 enrollments 表结构：status 改为枚举字符串，移除 week_freq/plan_status，created_at/updated_at 命名

-- 1. status: TINYINT -> VARCHAR(20)
ALTER TABLE enrollments ADD COLUMN status_new VARCHAR(20) NOT NULL DEFAULT 'NOT_STARTED' COMMENT 'NOT_STARTED/IN_PROGRESS/COMPLETED/EXPIRED';
UPDATE enrollments SET status_new = CASE status
    WHEN 0 THEN 'NOT_STARTED'
    WHEN 1 THEN 'IN_PROGRESS'
    WHEN 2 THEN 'COMPLETED'
    WHEN 3 THEN 'EXPIRED'
    ELSE 'NOT_STARTED'
END;
ALTER TABLE enrollments DROP COLUMN status;
ALTER TABLE enrollments CHANGE COLUMN status_new status VARCHAR(20) NOT NULL DEFAULT 'NOT_STARTED' COMMENT 'NOT_STARTED/IN_PROGRESS/COMPLETED/EXPIRED';

-- 2. 移除 week_freq、plan_status
ALTER TABLE enrollments DROP COLUMN week_freq;
ALTER TABLE enrollments DROP COLUMN plan_status;

-- 3. 重命名时间字段
ALTER TABLE enrollments CHANGE COLUMN create_time created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE enrollments CHANGE COLUMN update_time updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP;

-- 4. 更新唯一索引名称（保持语义一致）
ALTER TABLE enrollments DROP INDEX idx_user_id;
ALTER TABLE enrollments ADD UNIQUE KEY idx_user_course (user_id, course_id);
