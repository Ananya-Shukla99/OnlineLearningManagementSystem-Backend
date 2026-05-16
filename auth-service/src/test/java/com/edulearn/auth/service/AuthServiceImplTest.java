package com.edulearn.auth.service;

import com.edulearn.auth.entity.User;
import com.edulearn.auth.entity.UserRole;
import com.edulearn.auth.repository.UserRepository;
import com.edulearn.auth.util.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@DisplayName("AuthService Unit Tests")
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    private AuthServiceImpl authService;

    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        passwordEncoder = new BCryptPasswordEncoder();
        authService = new AuthServiceImpl(userRepository, jwtTokenProvider, passwordEncoder);
    }

    @Test
    @DisplayName("Register - Should successfully register a new user")
    void testRegisterSuccess() {
        // Arrange
        String email = "john@example.com";
        String fullName = "John Doe";
        String password = "password123";
        String role = "STUDENT";

        User user = new User();
        user.setUserId(1L);
        user.setEmail(email);
        user.setFullName(fullName);
        user.setRole(UserRole.STUDENT);

        when(userRepository.existsByEmail(email)).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(user);

        // Act
        User result = authService.register(email, fullName, password, role);

        // Assert
        assertNotNull(result);
        assertEquals(email, result.getEmail());
        assertEquals(fullName, result.getFullName());
        assertEquals(UserRole.STUDENT, result.getRole());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("Register - Should throw exception if email already exists")
    void testRegisterUserAlreadyExists() {
        // Arrange
        String email = "john@example.com";
        when(userRepository.existsByEmail(email)).thenReturn(true);

        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            authService.register(email, "John Doe", "password123", "STUDENT");
        });

        assertEquals("User already exists with email: " + email, exception.getReason());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Login - Should successfully login and return JWT token")
    void testLoginSuccess() {
        // Arrange
        String email = "john@example.com";
        String password = "password123";
        String token = "jwt_token_xyz";

        User user = new User();
        user.setUserId(1L);
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setRole(UserRole.STUDENT);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(jwtTokenProvider.generateToken(user)).thenReturn(token);

        // Act
        String result = authService.login(email, password);

        // Assert
        assertNotNull(result);
        assertEquals(token, result);
        verify(userRepository, times(1)).findByEmail(email);
        verify(jwtTokenProvider, times(1)).generateToken(user);
    }

    @Test
    @DisplayName("Login - Should throw exception if user not found")
    void testLoginUserNotFound() {
        // Arrange
        String email = "nonexistent@example.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            authService.login(email, "password123");
        });

        assertEquals("User not found with email: " + email, exception.getReason());
    }

    @Test
    @DisplayName("Login - Should throw exception if password is incorrect")
    void testLoginInvalidPassword() {
        // Arrange
        String email = "john@example.com";
        String correctPassword = "password123";
        String wrongPassword = "wrongpassword";

        User user = new User();
        user.setUserId(1L);
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(correctPassword));
        user.setRole(UserRole.STUDENT);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            authService.login(email, wrongPassword);
        });

        assertEquals("Invalid password", exception.getReason());
    }

    @Test
    @DisplayName("GetUserByEmail - Should return user if found")
    void testGetUserByEmailSuccess() {
        // Arrange
        String email = "john@example.com";
        User user = new User();
        user.setEmail(email);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        // Act
        Optional<User> result = authService.getUserByEmail(email);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(email, result.get().getEmail());
    }

    @Test
    @DisplayName("ValidateToken - Should validate token successfully")
    void testValidateTokenSuccess() {
        // Arrange
        String token = "valid_jwt_token";
        String userId = "1";

        when(jwtTokenProvider.validateToken(token)).thenReturn(userId);

        // Act
        String result = authService.validateToken(token);

        // Assert
        assertEquals(userId, result);
        verify(jwtTokenProvider, times(1)).validateToken(token);
    }

    @Test
    @DisplayName("Logout - Should complete successfully")
    void testLogout() {
        // Act & Assert
        assertDoesNotThrow(() -> authService.logout("some-token"));
    }

    @Test
    @DisplayName("RefreshToken - Should return new token")
    void testRefreshToken() {
        String oldToken = "old-token";
        String newToken = "new-token";
        User user = new User();
        user.setUserId(1L);

        when(jwtTokenProvider.validateToken(oldToken)).thenReturn("1");
        when(userRepository.findByUserId(1L)).thenReturn(Optional.of(user));
        when(jwtTokenProvider.generateToken(user)).thenReturn(newToken);

        String result = authService.refreshToken(oldToken);
        assertEquals(newToken, result);
    }

    @Test
    @DisplayName("GetUserById - Should return user")
    void testGetUserById() {
        User user = new User();
        user.setUserId(1L);
        when(userRepository.findByUserId(1L)).thenReturn(Optional.of(user));

        Optional<User> result = authService.getUserById(1L);
        assertTrue(result.isPresent());
    }

    @Test
    @DisplayName("ChangePassword - Should update password")
    void testChangePassword() {
        User user = new User();
        user.setUserId(1L);
        user.setPasswordHash(passwordEncoder.encode("oldPass"));

        when(userRepository.findByUserId(1L)).thenReturn(Optional.of(user));

        authService.changePassword(1L, "oldPass", "newPass");
        verify(userRepository).save(user);
    }

    @Test
    @DisplayName("UpdateProfile - Should update user info")
    void testUpdateProfile() {
        User user = new User();
        user.setUserId(1L);
        when(userRepository.findByUserId(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        User result = authService.updateProfile(1L, "New Name", "New Bio", "123456", "Headline", "Expertise");
        assertEquals("New Name", result.getFullName());
        assertEquals("New Bio", result.getBio());
    }

    @Test
    @DisplayName("DeleteUser - Should delete user")
    void testDeleteUser() {
        when(userRepository.existsById(1L)).thenReturn(true);
        authService.deleteUser(1L);
        verify(userRepository).deleteById(1L);
    }

    @Test
    @DisplayName("RefreshToken - Should throw exception if user not found")
    void testRefreshTokenUserNotFound() {
        when(jwtTokenProvider.validateToken(any())).thenReturn("1");
        when(userRepository.findByUserId(1L)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () -> authService.refreshToken("token"));
    }

    @Test
    @DisplayName("ChangePassword - Should throw exception if user not found")
    void testChangePasswordUserNotFound() {
        when(userRepository.findByUserId(1L)).thenReturn(Optional.empty());
        assertThrows(ResponseStatusException.class, () -> authService.changePassword(1L, "old", "new"));
    }

    @Test
    @DisplayName("ChangePassword - Should throw exception if old password incorrect")
    void testChangePasswordIncorrectOld() {
        User user = new User();
        user.setPasswordHash(passwordEncoder.encode("correct"));
        when(userRepository.findByUserId(1L)).thenReturn(Optional.of(user));

        assertThrows(ResponseStatusException.class, () -> authService.changePassword(1L, "wrong", "new"));
    }

    @Test
    @DisplayName("UpdateProfile - Should throw exception if user not found")
    void testUpdateProfileUserNotFound() {
        when(userRepository.findByUserId(1L)).thenReturn(Optional.empty());
        assertThrows(ResponseStatusException.class, () -> authService.updateProfile(1L, "name", "bio", "123", "head", "exp"));
    }

    @Test
    @DisplayName("DeleteUser - Should throw exception if user not found")
    void testDeleteUserNotFound() {
        when(userRepository.existsById(1L)).thenReturn(false);
        assertThrows(ResponseStatusException.class, () -> authService.deleteUser(1L));
    }
}
