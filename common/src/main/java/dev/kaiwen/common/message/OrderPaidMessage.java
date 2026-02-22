package dev.kaiwen.common.message;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 订单支付成功消息，order-service 发送，enrollment-service 消费。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderPaidMessage {

    private Long orderId;
    private Long userId;
    private Long courseId;
    /** 课程有效天数，null 表示永久 */
    private Integer validDays;
}
