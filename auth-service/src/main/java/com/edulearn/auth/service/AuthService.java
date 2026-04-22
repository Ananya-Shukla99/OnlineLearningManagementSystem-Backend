package com.edulearn.auth.service;

import com.edulearn.auth.entity.User;
import java.util.Optional;

public interface AuthService {

    User register(String email, String fullName, String password, String role);

    String login(String email, String password);

    void logout(String token);

    String validateToken(String token);

    String refreshToken(String token);

    Optional<User> getUserByEmail(String email);

    Optional<User> getUserById(Long userId);

    void changePassword(Long userId, String oldPassword, String newPassword);

    User updateProfile(Long userId, String fullName, String bio, String mobile);

    void deleteUser(Long userId);
}
