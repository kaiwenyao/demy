package dev.kaiwen.authservice.controller;

import dev.kaiwen.common.response.Result;
import dev.kaiwen.authservice.dto.LoginRequest;
import dev.kaiwen.authservice.dto.LoginResponse;
import dev.kaiwen.authservice.dto.RefreshRequest;
import dev.kaiwen.authservice.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Auth", description = "认证管理")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    @Operation(summary = "登录，支持邮箱或用户名")
    public Result<LoginResponse> login(@RequestBody @Valid LoginRequest request) {
        LoginResponse data = authService.login(request);
        return Result.success(data);
    }

    @PostMapping("/refresh")
    @Operation(summary = "刷新 Access Token")
    public Result<LoginResponse> refresh(@RequestBody @Valid RefreshRequest request) {
        LoginResponse data = authService.refresh(request);
        return Result.success(data);
    }
}
