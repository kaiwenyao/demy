package dev.kaiwen.orderservice.controller;

import dev.kaiwen.common.response.PageDto;
import dev.kaiwen.common.response.Result;
import dev.kaiwen.orderservice.dto.CreateOrderRequest;
import dev.kaiwen.orderservice.dto.OrderResponse;
import dev.kaiwen.orderservice.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
@Tag(name = "Order", description = "订单管理")
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    @Operation(summary = "创建订单")
    @ResponseStatus(HttpStatus.CREATED)
    public Result<OrderResponse> createOrder(
            @RequestBody @Valid CreateOrderRequest request,
            @RequestHeader("X-User-Id") Long userId
    ) {
        return Result.success(orderService.createOrder(userId, request.getCourseId()));
    }

    @PostMapping("/{orderId}/pay")
    @Operation(summary = "支付订单")
    public Result<OrderResponse> pay(
            @PathVariable Long orderId,
            @RequestHeader("X-User-Id") Long userId
    ) {
        return Result.success(orderService.payOrder(orderId, userId));
    }

    @GetMapping
    @Operation(summary = "查询我的订单列表")
    public Result<PageDto<OrderResponse>> myOrders(
            @RequestHeader("X-User-Id") Long userId,
            @ParameterObject Pageable pageable
    ) {
        return Result.success(orderService.findByUserId(userId, pageable));
    }
}
