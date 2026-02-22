package dev.kaiwen.userservice.controller;

import dev.kaiwen.common.response.Result;
import dev.kaiwen.userservice.dto.UserCredentialResponse;
import dev.kaiwen.userservice.entity.User;
import dev.kaiwen.userservice.repository.UserRepository;
import dev.kaiwen.userservice.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

/**
 * 内部接口，仅供 auth-service 通过 Feign 调用。
 * 不对外暴露，Gateway 需屏蔽 /internal/** 路径。
 */
@RestController
@RequestMapping("/internal/users")
@RequiredArgsConstructor
public class InternalUserController {

    private final UserRepository userRepository;
    private final UserService userService;

    @GetMapping("/by-id")
    public ResponseEntity<UserCredentialResponse> findById(@RequestParam Long id) {
        return userRepository.findById(id)
                .map(InternalUserController::toCredentialResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/by-email")
    public ResponseEntity<UserCredentialResponse> findByEmail(@RequestParam String email) {
        return userRepository.findByEmail(email)
                .map(InternalUserController::toCredentialResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/by-username")
    public ResponseEntity<UserCredentialResponse> findByUsername(@RequestParam String username) {
        return userRepository.findByUsername(username)
                .map(InternalUserController::toCredentialResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{userId}/deduct")
    public Result<Void> deductBalance(
            @PathVariable Long userId,
            @RequestParam BigDecimal amount
    ) {
        userService.deductBalance(userId, amount);
        return Result.success();
    }

    private static UserCredentialResponse toCredentialResponse(User user) {
        return new UserCredentialResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getPassword(),
                user.getRole() != null ? user.getRole() : "USER"
        );
    }
}
