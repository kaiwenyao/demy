-- 添加支付截止时间字段
ALTER TABLE orders ADD COLUMN pay_expire_time DATETIME NULL COMMENT '支付截止时间，超时自动取消' AFTER status;
UPDATE orders SET pay_expire_time = DATE_ADD(created_at, INTERVAL 30 MINUTE) WHERE pay_expire_time IS NULL;
ALTER TABLE orders MODIFY COLUMN pay_expire_time DATETIME NOT NULL COMMENT '支付截止时间，超时自动取消';
