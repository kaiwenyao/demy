package dev.kaiwen.common.message;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 余额扣款结果消息，user-service 发送，order-service 消费。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResultMessage {

    private Long orderId;
    private Long userId;
    private boolean success;
    private String failReason;
}

