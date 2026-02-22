package dev.kaiwen.authservice.service;

import dev.kaiwen.authservice.dto.LoginRequest;
import dev.kaiwen.authservice.dto.LoginResponse;
import dev.kaiwen.authservice.dto.RefreshRequest;

public interface AuthService {

    LoginResponse login(LoginRequest request);

    LoginResponse refresh(RefreshRequest request);
}
