package dev.kaiwen.userservice.repository;

import dev.kaiwen.userservice.entity.Outbox;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OutboxRepository extends JpaRepository<Outbox, Long> {

    List<Outbox> findByStatusAndRetryCountLessThan(String status, int retryCount);
}

