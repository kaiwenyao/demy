package dev.kaiwen.orderservice.consumer;

import dev.kaiwen.common.exception.ResourceNotFoundException;
import dev.kaiwen.common.message.OrderPaidMessage;
import dev.kaiwen.common.message.PaymentResultMessage;
import dev.kaiwen.orderservice.config.RabbitMQConfig;
import dev.kaiwen.orderservice.entity.Order;
import dev.kaiwen.orderservice.entity.OrderStatus;
import dev.kaiwen.orderservice.repository.OrderRepository;
import dev.kaiwen.orderservice.service.OrderService;
import dev.kaiwen.orderservice.service.OutboxService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentResultConsumer {

  private final OrderRepository orderRepository;
  private final OutboxService outboxService;


  @RabbitListener(queues = RabbitMQConfig.PAYMENT_RESULT_QUEUE)
  public void handlePaymentResult(PaymentResultMessage message) {
    Long orderId = message.getOrderId();
    Order order = orderRepository.findById(orderId).orElseThrow(
        () -> new ResourceNotFoundException("Order with id " + orderId + " not found")
    );
    if (message.isSuccess()) {
      order.setStatus(OrderStatus.PAID);
      orderRepository.save(order);
      outboxService.save(
          RabbitMQConfig.ORDER_EXCHANGE,
          RabbitMQConfig.ROUTING_KEY,
          new OrderPaidMessage(orderId, order.getUserId(), order.getCourseId(), order.getValidDays())
      );

      log.info("Order {} paid successfully", orderId);

    }
    else {
      order.setStatus(OrderStatus.FAILED);
      orderRepository.save(order);
      log.warn("Order payment {} failed, reason: {}", orderId, message.getFailReason());
    }

  }
}
