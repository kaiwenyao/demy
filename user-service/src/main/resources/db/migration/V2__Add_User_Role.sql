-- V2__Add_User_Role.sql
-- 添加 role 字段，默认值为 USER
ALTER TABLE users ADD COLUMN role VARCHAR(32) DEFAULT 'USER' COMMENT '用户角色' AFTER password;

-- 更新测试用户密码为 BCrypt 加密（明文 123456 的 hash）
UPDATE users SET password = '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', role = 'USER' WHERE username = 'kaiwen';
