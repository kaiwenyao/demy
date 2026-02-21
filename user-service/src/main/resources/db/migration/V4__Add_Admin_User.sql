-- V4__Add_Admin_User.sql
-- 添加管理员用户，用于课程管理（创建/更新/下架/添加小节）

-- 将 kaiwen 用户升级为管理员，或插入新管理员
-- 密码为 123456 的 BCrypt hash
UPDATE users SET role = 'ROLE_ADMIN' WHERE username = 'kaiwen';
