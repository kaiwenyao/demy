package dev.kaiwen.orderservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;

@FeignClient(name = "user-service")
public interface UserServiceClient {

    @PostMapping("/internal/users/{userId}/deduct")
    void deductBalance(
            @PathVariable("userId") Long userId,
            @RequestParam("amount") BigDecimal amount
    );
}
