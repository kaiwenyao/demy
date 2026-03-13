package dev.kaiwen.orderservice.repository;

import dev.kaiwen.orderservice.entity.Outbox;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OutboxRepository extends JpaRepository<Outbox, Long> {
  List<Outbox> findByStatusAndRetryCountLessThan(String status, int retryCount);
}

