package dev.kaiwen.orderservice.dto;

import dev.kaiwen.orderservice.entity.OrderStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Schema(description = "订单响应")
public class OrderResponse {

    private Long id;
    private Long userId;
    private Long courseId;
    private BigDecimal amount;
    private OrderStatus status;
    private Instant payExpireTime;
    private Instant createdAt;
}
