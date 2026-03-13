package dev.kaiwen.userservice.scheduler;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.kaiwen.userservice.entity.Outbox;
import dev.kaiwen.userservice.repository.OutboxRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class OutboxScheduler {

    private static final String STATUS_PENDING = "PENDING";
    private static final String STATUS_SENT = "SENT";

    private final OutboxRepository outboxRepository;
    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;

    @Scheduled(fixedDelay = 5000)
    @Transactional
    public void processOutbox() {
        List<Outbox> messages = outboxRepository.findByStatusAndRetryCountLessThan(STATUS_PENDING, 3);
        if (messages.isEmpty()) {
            return;
        }

        for (Outbox outbox : messages) {
            try {
                Object payload = objectMapper.readValue(outbox.getPayload(), Object.class);
                rabbitTemplate.convertAndSend(outbox.getExchange(), outbox.getRoutingKey(), payload);

                outbox.setStatus(STATUS_SENT);
                outbox.setSentAt(Instant.now());
                outboxRepository.save(outbox);
            } catch (Exception e) {
                log.error("Failed to send outbox message id={}", outbox.getId(), e);

                outbox.setRetryCount(outbox.getRetryCount() + 1);
                outbox.setLastRetryAt(Instant.now());
                outboxRepository.save(outbox);
            }
        }
    }
}

