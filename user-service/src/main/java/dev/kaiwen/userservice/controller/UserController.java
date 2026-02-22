package dev.kaiwen.userservice.controller;

import dev.kaiwen.common.response.Result;
import dev.kaiwen.userservice.dto.RegisterRequest;
import dev.kaiwen.userservice.dto.UserResponse;
import dev.kaiwen.userservice.entity.User;
import dev.kaiwen.userservice.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "User", description = "用户管理")
public class UserController {

    private final UserService userService;

    @PostMapping("/register")
    @Operation(summary = "用户注册")
    @ResponseStatus(HttpStatus.CREATED)
    public Result<UserResponse> register(@RequestBody @Valid RegisterRequest request) {
        User user = userService.register(request);
        return Result.created(UserResponse.from(user));
    }
}
