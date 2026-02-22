package dev.kaiwen.enrollmentservice.consumer;

import dev.kaiwen.common.message.OrderPaidMessage;
import dev.kaiwen.enrollmentservice.service.EnrollmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderPaidConsumer {

    private final EnrollmentService enrollmentService;

    @RabbitListener(queues = "enrollment.queue")
    public void handleOrderPaid(OrderPaidMessage message) {
        log.info("Received order paid message: {}", message);
        try {
            enrollmentService.createFromOrder(message);
        } catch (Exception e) {
            log.error("Failed to process order paid message: {}", message, e);
            throw e; // 抛出异常会触发重试
        }
    }
}
