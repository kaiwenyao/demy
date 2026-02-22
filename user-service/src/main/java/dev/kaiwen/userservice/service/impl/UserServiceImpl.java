package dev.kaiwen.userservice.service.impl;

import dev.kaiwen.common.exception.BadRequestException;
import dev.kaiwen.common.exception.ResourceAlreadyExistsException;
import dev.kaiwen.common.exception.ResourceNotFoundException;
import dev.kaiwen.userservice.dto.RegisterRequest;
import dev.kaiwen.userservice.entity.User;
import dev.kaiwen.userservice.repository.UserRepository;
import dev.kaiwen.userservice.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public User register(RegisterRequest request) {
        String username = request.getUsername() != null ? request.getUsername().trim() : "";
        if (username.isBlank()) {
            throw new BadRequestException("Username is required");
        }
        if (request.getPassword() == null || request.getPassword().isBlank()) {
            throw new BadRequestException("Password is required");
        }
        if (userRepository.existsByUsername(username)) {
            throw new ResourceAlreadyExistsException("Username already exists");
        }
        String email = request.getEmail() != null ? request.getEmail().trim() : null;
        if (email != null && !email.isBlank() && userRepository.existsByEmail(email)) {
            throw new ResourceAlreadyExistsException("Email already registered");
        }
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole("USER");
        return userRepository.save(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deductBalance(Long userId, BigDecimal amount) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (user.getBalance() == null || user.getBalance().compareTo(amount) < 0) {
            throw new BadRequestException("Insufficient balance");
        }

        user.setBalance(user.getBalance().subtract(amount));
        userRepository.save(user);
    }
}
