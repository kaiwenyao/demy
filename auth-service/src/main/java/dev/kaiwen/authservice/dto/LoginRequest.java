package dev.kaiwen.authservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 登录请求，邮箱和用户名共用一个字段
 */
@Data
@Schema(description = "登录请求")
public class LoginRequest {

    @NotBlank
    @Schema(description = "邮箱或用户名", example = "user@example.com")
    private String identifier;   // 用户名 or 邮箱，统一一个字段

    @NotBlank
    @Schema(description = "密码")
    private String password;
}
