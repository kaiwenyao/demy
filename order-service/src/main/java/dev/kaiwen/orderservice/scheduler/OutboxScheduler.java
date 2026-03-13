package dev.kaiwen.orderservice.scheduler;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.kaiwen.orderservice.entity.Outbox;
import dev.kaiwen.orderservice.repository.OutboxRepository;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
@Slf4j
public class OutboxScheduler {

  private final OutboxRepository outboxRepository;
  private final RabbitTemplate rabbitTemplate;
  private final ObjectMapper objectMapper;

  @Scheduled(fixedDelay = 5000)
  public void processOutbox() {
    List<Outbox> messages = outboxRepository.findByStatusAndRetryCountLessThan("PENDING", 3);

    for (Outbox outbox : messages) {
      try {
        Object payload = objectMapper.readValue(outbox.getPayload(), Object.class);
        rabbitTemplate.convertAndSend(
            outbox.getExchange(),
            outbox.getRoutingKey(),
            payload
        );

        outbox.setStatus("SENT");
        outbox.setSentAt(Instant.now());
        log.info("Outbox sent successfully: {}", outbox.getId());
      } catch (Exception e) {
        outbox.setRetryCount(outbox.getRetryCount() + 1);
        outbox.setLastRetryAt(Instant.now());
        log.error("Outbox sent failed: {}, retry count: {}", outbox.getId(), outbox.getRetryCount(),
            e);
      }
      outboxRepository.save(outbox);
    }

  }
}
