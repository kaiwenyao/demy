package dev.kaiwen.orderservice.repository;

import dev.kaiwen.orderservice.entity.Order;
import dev.kaiwen.orderservice.entity.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {

    boolean existsByUserIdAndCourseIdAndStatus(Long userId, Long courseId, OrderStatus status);

    Page<Order> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
}
