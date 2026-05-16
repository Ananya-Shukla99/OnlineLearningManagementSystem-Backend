package com.edulearn.auth.service;

import com.edulearn.auth.entity.User;
import com.edulearn.auth.entity.UserRole;
import com.edulearn.auth.repository.UserRepository;
import com.edulearn.auth.util.JwtTokenProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

@Service
public class AuthServiceImpl implements AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthServiceImpl.class);
    private static final String USER_NOT_FOUND = "User not found";

    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;

    public AuthServiceImpl(UserRepository userRepository, JwtTokenProvider jwtTokenProvider, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.jwtTokenProvider = jwtTokenProvider;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public User register(String email, String fullName, String password, String role) {
        // Check if user already exists
        if (userRepository.existsByEmail(email)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User already exists with email: " + email);
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
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found with email: " + email);
        }

        // Verify password
        if (!passwordEncoder.matches(password, user.get().getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid password");
        }

        // Generate JWT token
        return jwtTokenProvider.generateToken(user.get());
    }

    @Override
    public void logout(String token) {
        // In a real app, you might add the token to a blacklist
        logger.info("User logged out. Token invalidated: {}", token);
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
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, USER_NOT_FOUND);
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
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, USER_NOT_FOUND);
        }

        // Verify old password
        if (!passwordEncoder.matches(oldPassword, user.get().getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Old password is incorrect");
        }

        // Update password
        user.get().setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user.get());
    }

    @Override
    public User updateProfile(Long userId, String fullName, String bio, String mobile, String headline, String expertise) {
        Optional<User> user = userRepository.findByUserId(userId);

        if (user.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, USER_NOT_FOUND);
        }

        User updatedUser = user.get();
        updatedUser.setFullName(fullName);
        updatedUser.setBio(bio);
        updatedUser.setMobile(mobile);
        updatedUser.setHeadline(headline);
        updatedUser.setExpertise(expertise);

        return userRepository.save(updatedUser);
    }

    @Override
    public void deleteUser(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, USER_NOT_FOUND);
        }
        userRepository.deleteById(userId);
    }
}
