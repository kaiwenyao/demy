package dev.kaiwen.userservice.service.impl;

import dev.kaiwen.common.exception.BadRequestException;
import dev.kaiwen.common.exception.ResourceAlreadyExistsException;
import dev.kaiwen.userservice.dto.RegisterRequest;
import dev.kaiwen.userservice.entity.User;
import dev.kaiwen.userservice.repository.UserRepository;
import dev.kaiwen.userservice.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        return userRepository.save(user);
    }
}
