package dev.kaiwen.authservice.client;

import dev.kaiwen.authservice.dto.UserCredentialResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "user-service")
public interface UserServiceClient {

    @GetMapping("/internal/users/by-email")
    UserCredentialResponse findByEmail(@RequestParam("email") String email);

    @GetMapping("/internal/users/by-username")
    UserCredentialResponse findByUsername(@RequestParam("username") String username);
}
