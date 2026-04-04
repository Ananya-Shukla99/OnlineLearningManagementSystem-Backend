package com.edulearn.auth.service;

import com.edulearn.auth.entity.User;
import com.edulearn.auth.entity.UserRole;
import com.edulearn.auth.repository.UserRepository;
import com.edulearn.auth.util.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
public class AuthServiceImpl implements AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    private BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public User register(String email, String fullName, String password, String role) {
        // Check if user already exists
        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("User already exists with email: " + email);
        }

        // Create new user
        User user = new User();
        user.setEmail(email);
        user.setFullName(fullName);
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setRole(UserRole.valueOf(role.toUpperCase()));
        user.setProvider("local");

        return userRepository.save(user);
    }

    @Override
    public String login(String email, String password) {
        Optional<User> user = userRepository.findByEmail(email);

        if (user.isEmpty()) {
            throw new RuntimeException("User not found with email: " + email);
        }

        // Verify password
        if (!passwordEncoder.matches(password, user.get().getPasswordHash())) {
            throw new RuntimeException("Invalid password");
        }

        // Generate JWT token
        return jwtTokenProvider.generateToken(user.get());
    }

    @Override
    public void logout(String token) {
        // In a real app, you might add the token to a blacklist
        System.out.println("User logged out. Token invalidated: " + token);
    }

    @Override
    public String validateToken(String token) {
        return jwtTokenProvider.validateToken(token);
    }

    @Override
    public String refreshToken(String token) {
        // Validate current token first
        String userId = jwtTokenProvider.validateToken(token);

        Optional<User> user = userRepository.findByUserId(Long.parseLong(userId));
        if (user.isEmpty()) {
            throw new RuntimeException("User not found");
        }

        // Generate new token
        return jwtTokenProvider.generateToken(user.get());
    }

    @Override
    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    public Optional<User> getUserById(Long userId) {
        return userRepository.findByUserId(userId);
    }

    @Override
    public void changePassword(Long userId, String oldPassword, String newPassword) {
        Optional<User> user = userRepository.findByUserId(userId);

        if (user.isEmpty()) {
            throw new RuntimeException("User not found");
        }

        // Verify old password
        if (!passwordEncoder.matches(oldPassword, user.get().getPasswordHash())) {
            throw new RuntimeException("Old password is incorrect");
        }

        // Update password
        user.get().setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user.get());
    }

    @Override
    public User updateProfile(Long userId, String fullName, String bio, String mobile) {
        Optional<User> user = userRepository.findByUserId(userId);

        if (user.isEmpty()) {
            throw new RuntimeException("User not found");
        }

        User updatedUser = user.get();
        updatedUser.setFullName(fullName);
        updatedUser.setBio(bio);
        updatedUser.setMobile(mobile);

        return userRepository.save(updatedUser);
    }

    @Override
    public void deleteUser(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new RuntimeException("User not found");
        }
        userRepository.deleteById(userId);
    }
}
