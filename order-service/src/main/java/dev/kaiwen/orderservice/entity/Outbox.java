package dev.kaiwen.orderservice.entity;

import io.hypersistence.tsid.TSID;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Table(name = "outbox")
@Getter
@Setter
public class Outbox {

    @Id
    private Long id;

    @Column(nullable = false, length = 100)
    private String exchange;

    @Column(name = "routing_key", nullable = false, length = 100)
    private String routingKey;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String payload;

    @Column(nullable = false, length = 20)
    private String status = "PENDING";

    @Column(name = "retry_count", nullable = false)
    private Integer retryCount = 0;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @Column(name = "sent_at")
    private Instant sentAt;

    @Column(name = "last_retry_at")
    private Instant lastRetryAt;

    @PrePersist
    private void prePersist() {
        if (this.id == null) {
            this.id = TSID.fast().toLong();
        }
    }
}
