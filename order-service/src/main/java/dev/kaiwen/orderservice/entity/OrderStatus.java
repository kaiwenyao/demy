package dev.kaiwen.orderservice.entity;

/**
 * 订单状态枚举
 */
public enum OrderStatus {
    PENDING,   // 待支付
    PAID,      // 已支付
    CANCELLED  // 已取消
}
