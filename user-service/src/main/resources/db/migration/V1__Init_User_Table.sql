-- V1__Init_User_Table.sql

-- 创建用户表
CREATE TABLE IF NOT EXISTS users
(
    id
    BIGINT
    AUTO_INCREMENT
    PRIMARY
    KEY
    COMMENT
    '主键ID',
    username
    VARCHAR
(
    64
) NOT NULL COMMENT '用户名',
    email VARCHAR
(
    128
) COMMENT '邮箱',
    password VARCHAR
(
    128
) NOT NULL COMMENT '密码',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 初始化一条测试数据
INSERT INTO users (username, email, password)
VALUES ('kaiwen', 'kaiwen@example.com', '123456');