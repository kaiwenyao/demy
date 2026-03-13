package dev.kaiwen.userservice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.kaiwen.userservice.entity.Outbox;
import dev.kaiwen.userservice.repository.OutboxRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class OutboxService {

    private final OutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;

    public void save(String exchange, String routingKey, Object payload) {
        try {
            Outbox outbox = new Outbox();
            outbox.setExchange(exchange);
            outbox.setRoutingKey(routingKey);
            outbox.setPayload(objectMapper.writeValueAsString(payload));
            outboxRepository.save(outbox);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize outbox payload for exchange={}, routingKey={}",
                    exchange, routingKey, e);
            throw new RuntimeException("System error during outbox processing", e);
        }
    }
}

