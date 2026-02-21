package dev.kaiwen.userservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 内部接口返回的 DTO，供 auth-service 调用时使用。
 * 包含 BCrypt 加密后的密码，用于认证校验。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserCredentialResponse {

    private Long id;
    private String username;
    private String email;
    private String password;   // bcrypt 加密后的密码，auth-service 用来比对
    private String role;
}
