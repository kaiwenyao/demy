package dev.kaiwen.common.message;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 余额扣款请求消息，order-service 发送，user-service 消费。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentRequestMessage {

    private Long orderId;
    private Long userId;
    private BigDecimal amount;
}

