package dev.kaiwen.userservice.controller;

import dev.kaiwen.common.exception.ResourceNotFoundException;
import dev.kaiwen.userservice.dto.UserCredentialResponse;
import dev.kaiwen.userservice.entity.User;
import dev.kaiwen.userservice.repository.UserRepository;
import dev.kaiwen.userservice.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

/**
 * 内部接口，供 auth-service、order-service 等通过 Feign 调用。
 * 不对外暴露，Gateway 需屏蔽 /internal/** 路径。
 * 直接返回 DTO，不封装 ResponseEntity 或 Result。
 */
@RestController
@RequestMapping("/internal/users")
@RequiredArgsConstructor
public class InternalUserController {

    private final UserRepository userRepository;
    private final UserService userService;

    @GetMapping("/by-id")
    public UserCredentialResponse findById(@RequestParam Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + id));
        return toCredentialResponse(user);
    }

    @GetMapping("/by-email")
    public UserCredentialResponse findByEmail(@RequestParam String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));
        return toCredentialResponse(user);
    }

    @GetMapping("/by-username")
    public UserCredentialResponse findByUsername(@RequestParam String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
        return toCredentialResponse(user);
    }

    @PostMapping("/{userId}/deduct")
    public void deductBalance(
            @PathVariable Long userId,
            @RequestParam BigDecimal amount
    ) {
        userService.deductBalance(userId, amount);
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
