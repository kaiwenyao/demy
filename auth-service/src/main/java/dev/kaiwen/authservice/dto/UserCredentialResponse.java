package dev.kaiwen.authservice.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Feign 调用 user-service 内部接口返回的 DTO。
 * 与 user-service 的 UserCredentialResponse 结构一致。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserCredentialResponse {

    private Long id;
    private String username;
    private String email;
    private String password;   // bcrypt 加密后的密码，用于比对
    private String role;
}
