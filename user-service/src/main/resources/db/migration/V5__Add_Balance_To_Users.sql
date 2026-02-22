-- V5__Add_Balance_To_Users.sql
ALTER TABLE users ADD COLUMN balance DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '账户余额';
