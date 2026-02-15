package dev.kaiwen.userservice.service;

import dev.kaiwen.userservice.dto.RegisterRequest;
import dev.kaiwen.userservice.entity.User;

public interface UserService {

    User register(RegisterRequest request);
}
