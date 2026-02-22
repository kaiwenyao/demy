package dev.kaiwen.orderservice.service;

import dev.kaiwen.common.response.PageDto;
import dev.kaiwen.orderservice.dto.OrderResponse;
import org.springframework.data.domain.Pageable;

public interface OrderService {

    /**
     * 创建订单
     */
    OrderResponse createOrder(Long userId, Long courseId);

    /**
     * 支付订单（余额扣款）
     */
    OrderResponse payOrder(Long orderId, Long userId);

    /**
     * 分页查询我的订单列表
     */
    PageDto<OrderResponse> findByUserId(Long userId, Pageable pageable);
}
