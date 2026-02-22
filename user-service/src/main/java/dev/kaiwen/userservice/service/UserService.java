package dev.kaiwen.userservice.service;

import dev.kaiwen.userservice.dto.RegisterRequest;
import dev.kaiwen.userservice.entity.User;

import java.math.BigDecimal;

public interface UserService {

    User register(RegisterRequest request);

    void deductBalance(Long userId, BigDecimal amount);
}
