package dev.kaiwen.orderservice.entity;

/**
 * 订单状态枚举
 */
public enum OrderStatus {
    PENDING,   // 待支付
    PAYING,    // 支付中
    PAID,      // 已支付
    FAILED,    // 支付失败
    CANCELLED  // 已取消
}
