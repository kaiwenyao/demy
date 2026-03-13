package dev.kaiwen.userservice.consumer;

import dev.kaiwen.common.message.PaymentRequestMessage;
import dev.kaiwen.common.message.PaymentResultMessage;
import dev.kaiwen.userservice.config.RabbitMQConfig;
import dev.kaiwen.userservice.service.OutboxService;
import dev.kaiwen.userservice.service.UserService;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Slf4j
@Component
public class PaymentRequestConsumer {

  private final UserService userService;
  private final OutboxService outboxService;

  @RabbitListener(queues = RabbitMQConfig.PAYMENT_REQUEST_QUEUE)
  public void handlePaymentRequest(PaymentRequestMessage message) {
    Long userId = message.getUserId();
    Long orderId = message.getOrderId();
    BigDecimal amount = message.getAmount();
    log.info("Handling payment request for user {} and order {}", userId, orderId);
    try {
      userService.deductBalance(userId, amount);
      outboxService.save(
          RabbitMQConfig.PAYMENT_RESULT_EXCHANGE,
          RabbitMQConfig.PAYMENT_RESULT_ROUTING_KEY,
          new PaymentResultMessage(orderId, userId, true, null)
          );
      log.info("Payment result for user {} and order {}", userId, orderId);
    }
    catch (Exception e) {
      log.error("Error handling payment request for user {} and order {}", userId, orderId, e);
      outboxService.save(
          RabbitMQConfig.PAYMENT_RESULT_EXCHANGE,
          RabbitMQConfig.PAYMENT_RESULT_ROUTING_KEY,
          new PaymentResultMessage(orderId, userId, false, e.getMessage())
      );
    }
  }
}
