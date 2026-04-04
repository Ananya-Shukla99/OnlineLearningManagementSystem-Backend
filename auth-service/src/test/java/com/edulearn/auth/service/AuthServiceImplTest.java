package com.edulearn.auth.service;

import com.edulearn.auth.entity.User;
import com.edulearn.auth.entity.UserRole;
import com.edulearn.auth.repository.UserRepository;
import com.edulearn.auth.util.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

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

    @InjectMocks
    private AuthServiceImpl authService;

    private BCryptPasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        passwordEncoder = new BCryptPasswordEncoder();
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
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authService.register(email, "John Doe", "password123", "STUDENT");
        });

        assertEquals("User already exists with email: " + email, exception.getMessage());
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
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authService.login(email, "password123");
        });

        assertEquals("User not found with email: " + email, exception.getMessage());
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
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authService.login(email, wrongPassword);
        });

        assertEquals("Invalid password", exception.getMessage());
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
}
