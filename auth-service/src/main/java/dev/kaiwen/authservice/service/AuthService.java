package dev.kaiwen.authservice.service;

import dev.kaiwen.authservice.dto.LoginRequest;
import dev.kaiwen.authservice.dto.LoginResponse;
import dev.kaiwen.authservice.dto.RefreshRequest;
import dev.kaiwen.authservice.dto.UserCredentialResponse;
import dev.kaiwen.authservice.client.UserServiceClient;
import dev.kaiwen.common.exception.BadRequestException;
import dev.kaiwen.common.exception.ResourceNotFoundException;
import feign.FeignException;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserServiceClient userServiceClient;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public LoginResponse login(LoginRequest request) {
        UserCredentialResponse user;
        try {
            user = isEmail(request.getIdentifier())
                    ? userServiceClient.findByEmail(request.getIdentifier())
                    : userServiceClient.findByUsername(request.getIdentifier());
        } catch (FeignException.NotFound e) {
            // 用户不存在时，与密码错误统一返回相同错误，防止枚举攻击
            throw new BadCredentialsException("Invalid credentials");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BadCredentialsException("Invalid credentials");
        }

        String accessToken = jwtService.generateAccessToken(
                user.getId(), user.getEmail(), user.getRole() != null ? user.getRole() : "USER"
        );
        String refreshToken = jwtService.generateRefreshToken(user.getId());

        return new LoginResponse(accessToken, refreshToken);
    }

    public LoginResponse refresh(RefreshRequest request) {
        Claims claims;
        try {
            claims = jwtService.parseToken(request.getRefreshToken());
        } catch (Exception e) {
            throw new BadRequestException("Invalid refresh token");
        }

        String type = claims.get("type", String.class);
        if (!"refresh".equals(type)) {
            throw new BadRequestException("Invalid refresh token");
        }

        Long userId = Long.valueOf(claims.getSubject());

        UserCredentialResponse user;
        try {
            user = userServiceClient.findById(userId);
        } catch (FeignException.NotFound e) {
            throw new ResourceNotFoundException("User not found");
        }

        String newAccessToken = jwtService.generateAccessToken(
                user.getId(), user.getEmail(), user.getRole() != null ? user.getRole() : "USER"
        );
        String newRefreshToken = jwtService.generateRefreshToken(user.getId());

        return new LoginResponse(newAccessToken, newRefreshToken);
    }

    private boolean isEmail(String identifier) {
        return identifier != null && identifier.contains("@");
    }
}
