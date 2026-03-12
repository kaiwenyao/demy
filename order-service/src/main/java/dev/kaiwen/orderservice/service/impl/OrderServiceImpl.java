package dev.kaiwen.orderservice.service.impl;

import dev.kaiwen.common.dto.CourseInternalResponse;
import dev.kaiwen.common.exception.BadRequestException;
import dev.kaiwen.common.exception.ResourceAlreadyExistsException;
import dev.kaiwen.common.exception.ResourceNotFoundException;
import dev.kaiwen.common.message.OrderPaidMessage;
import dev.kaiwen.common.response.PageDto;
import dev.kaiwen.orderservice.client.CourseServiceClient;
import dev.kaiwen.orderservice.client.UserServiceClient;
import dev.kaiwen.orderservice.config.RabbitMQConfig;
import dev.kaiwen.orderservice.dto.OrderResponse;
import dev.kaiwen.orderservice.entity.Order;
import dev.kaiwen.orderservice.entity.OrderStatus;
import dev.kaiwen.orderservice.repository.OrderRepository;
import dev.kaiwen.orderservice.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final CourseServiceClient courseServiceClient;
    private final UserServiceClient userServiceClient;
    private final RabbitTemplate rabbitTemplate;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrderResponse createOrder(Long userId, Long courseId) {

        // 1. 检查是否已购买
        if (orderRepository.existsByUserIdAndCourseIdAndStatus(
                userId, courseId, OrderStatus.PAID)) {
            throw new ResourceAlreadyExistsException("Course already purchased");
        }

        // 2. 检查是否有未支付的订单
        if (orderRepository.existsByUserIdAndCourseIdAndStatus(
                userId, courseId, OrderStatus.PENDING)) {
            throw new ResourceAlreadyExistsException("Order already exists, please pay");
        }

        // 3. 查询课程信息（内部接口不查小节，仅返回基础字段）
        CourseInternalResponse course = courseServiceClient.getCourseById(courseId);
        if (course == null) {
            throw new ResourceNotFoundException("Course not found: " + courseId);
        }
        if (!"ACTIVE".equals(course.getStatus())) {
            throw new ResourceNotFoundException("Course not found: " + courseId);
        }

        BigDecimal price = course.getPrice() != null ? course.getPrice() : BigDecimal.ZERO;

        // 4. 创建订单
        Order order = new Order();
        order.setUserId(userId);
        order.setCourseId(courseId);
        order.setAmount(price);
        order.setStatus(OrderStatus.PENDING);
        order.setPayExpireTime(Instant.now().plusSeconds(30 * 60));
        orderRepository.save(order);

        return toResponse(order);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrderResponse payOrder(Long orderId, Long userId) {

        // 1. 查询订单
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Order not found: " + orderId));

        // 2. 校验归属
        if (!order.getUserId().equals(userId)) {
            throw new BadRequestException("Order does not belong to you");
        }

        // 3. 校验状态
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new BadRequestException("Order is not in PENDING status");
        }

        // 4. 校验是否超时
        if (Instant.now().isAfter(order.getPayExpireTime())) {
            order.setStatus(OrderStatus.CANCELLED);
            orderRepository.save(order);
            throw new BadRequestException("Order has expired");
        }

        // 5. 付费课程扣余额，免费课程不扣
        if (order.getAmount().compareTo(BigDecimal.ZERO) > 0) {
            try {
                userServiceClient.deductBalance(userId, order.getAmount());
            } catch (Exception e) {
                throw new BadRequestException("Insufficient balance");
            }
        }

        // 6. 更新订单状态
        order.setStatus(OrderStatus.PAID);
        orderRepository.save(order);

        // 7. 查询 validDays 发送 MQ
        CourseInternalResponse course = courseServiceClient.getCourseById(order.getCourseId());
        Integer validDays = course != null ? course.getValidDays() : null;

        rabbitTemplate.convertAndSend(
                RabbitMQConfig.ORDER_EXCHANGE,
                RabbitMQConfig.ROUTING_KEY,
                new OrderPaidMessage(
                        order.getId(),
                        order.getUserId(),
                        order.getCourseId(),
                        validDays
                )
        );
        log.info("Order {} paid successfully", orderId);

        return toResponse(order);
    }

    @Override
    public PageDto<OrderResponse> findByUserId(Long userId, Pageable pageable) {
        Page<Order> page = orderRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
        List<OrderResponse> content = page.getContent().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
        return PageDto.of(content, page.getTotalElements(), page.getNumber(), page.getSize());
    }

    private OrderResponse toResponse(Order order) {
        OrderResponse response = new OrderResponse();
        response.setId(order.getId());
        response.setUserId(order.getUserId());
        response.setCourseId(order.getCourseId());
        response.setAmount(order.getAmount());
        response.setStatus(order.getStatus());
        response.setPayExpireTime(order.getPayExpireTime());
        response.setCreatedAt(order.getCreatedAt());
        return response;
    }
}
