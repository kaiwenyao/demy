-- V2__Simplify_Courses_Table.sql
-- 简化课程表：去掉审核流程，status 改为 ACTIVE/INACTIVE

ALTER TABLE courses DROP COLUMN reject_reason;

-- 迁移旧状态：PUBLISHED -> ACTIVE，其余 -> INACTIVE
UPDATE courses SET status = 'ACTIVE' WHERE status = 'PUBLISHED';
UPDATE courses SET status = 'INACTIVE' WHERE status IN ('DRAFT', 'UNDER_REVIEW', 'REJECTED');

ALTER TABLE courses MODIFY COLUMN status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE';
