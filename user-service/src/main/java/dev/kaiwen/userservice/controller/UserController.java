package dev.kaiwen.userservice.controller;

import dev.kaiwen.common.pojo.Result;
import dev.kaiwen.userservice.dto.RegisterRequest;
import dev.kaiwen.userservice.entity.User;
import dev.kaiwen.userservice.service.UserService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public Result<User> register(@RequestBody RegisterRequest request) {
        User user = userService.register(request);
        return Result.success(user);
    }
}
