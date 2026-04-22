package com.edulearn.auth.controller;

import com.edulearn.auth.dto.AuthResponse;
import com.edulearn.auth.dto.ChangePasswordRequest;
import com.edulearn.auth.dto.LoginRequest;
import com.edulearn.auth.dto.RegisterRequest;
import com.edulearn.auth.dto.UpdateProfileRequest;
import com.edulearn.auth.dto.UserSummaryDTO;
import com.edulearn.auth.entity.User;
import com.edulearn.auth.entity.UserRole;
import com.edulearn.auth.repository.UserRepository;
import com.edulearn.auth.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/auth")

@Tag(name = "Authentication", description = "Authentication and Authorization API endpoints")
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private UserRepository userRepository;

    // ─── Helper: User → UserSummaryDTO ──────────────────────────────────────
    private UserSummaryDTO toSummaryDTO(User user) {
        return new UserSummaryDTO(
                user.getUserId(),
                user.getEmail(),
                user.getFullName(),
                user.getRole().toString(),
                user.getBio(),
                user.getMobile(),
                user.getCreatedAt()
        );
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // PUBLIC ENDPOINTS
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * POST /auth/register
     * Register a new user
     */
    @PostMapping("/register")
    @Operation(summary = "Register a new user", description = "Create a new user account with email, password, and role")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "User registered successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AuthResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid registration request")
    })
    public ResponseEntity<AuthResponse> register(@RequestBody RegisterRequest request) {
        try {
            User user = authService.register(
                    request.getEmail(),
                    request.getFullName(),
                    request.getPassword(),
                    request.getRole()
            );

            AuthResponse response = new AuthResponse();
            response.setSuccess(true);
            response.setMessage("User registered successfully");
            response.setUserId(user.getUserId());
            response.setEmail(user.getEmail());
            response.setFullName(user.getFullName());
            response.setRole(user.getRole().toString());

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (RuntimeException e) {
            AuthResponse response = new AuthResponse();
            response.setSuccess(false);
            response.setMessage(e.getMessage());

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    /**
     * POST /auth/login
     * Login user and return JWT token
     */
    @PostMapping("/login")
    @Operation(summary = "User login", description = "Authenticate user with email and password, returns JWT token")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Login successful, JWT token returned", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AuthResponse.class))),
        @ApiResponse(responseCode = "401", description = "Invalid credentials")
    })
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        try {
            String token = authService.login(request.getEmail(), request.getPassword());
            User user = authService.getUserByEmail(request.getEmail()).orElseThrow();

            AuthResponse response = new AuthResponse();
            response.setSuccess(true);
            response.setMessage("Login successful");
            response.setToken(token);
            response.setUserId(user.getUserId());
            response.setEmail(user.getEmail());
            response.setFullName(user.getFullName());
            response.setRole(user.getRole().toString());

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            AuthResponse response = new AuthResponse();
            response.setSuccess(false);
            response.setMessage(e.getMessage());

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
    }

    /**
     * GET /auth/validate?token=xxx
     * Validate JWT token
     */
    @GetMapping("/validate")
    @Operation(summary = "Validate JWT token", description = "Verify if the provided JWT token is valid")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Token is valid", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AuthResponse.class))),
        @ApiResponse(responseCode = "401", description = "Invalid or expired token")
    })
    public ResponseEntity<AuthResponse> validateToken(@RequestParam String token) {
        try {
            String userId = authService.validateToken(token);

            AuthResponse response = new AuthResponse();
            response.setSuccess(true);
            response.setMessage("Token is valid");
            response.setUserId(Long.parseLong(userId));

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            AuthResponse response = new AuthResponse();
            response.setSuccess(false);
            response.setMessage(e.getMessage());

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
    }

    /**
     * POST /auth/refresh?token=xxx
     * Refresh JWT token
     */
    @PostMapping("/refresh")
    @Operation(summary = "Refresh JWT token", description = "Generate a new JWT token from an existing valid token")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Token refreshed successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = AuthResponse.class))),
        @ApiResponse(responseCode = "401", description = "Invalid or expired token")
    })
    public ResponseEntity<AuthResponse> refreshToken(@RequestParam String token) {
        try {
            String newToken = authService.refreshToken(token);

            AuthResponse response = new AuthResponse();
            response.setSuccess(true);
            response.setMessage("Token refreshed successfully");
            response.setToken(newToken);

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            AuthResponse response = new AuthResponse();
            response.setSuccess(false);
            response.setMessage(e.getMessage());

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // AUTHENTICATED ENDPOINTS (Backend Gap 1 & 5)
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * GET /auth/me
     * Get current user info from JWT
     */
    @GetMapping("/me")
    @Operation(summary = "Get current user", description = "Retrieve the currently authenticated user's profile using the JWT token")
    public ResponseEntity<AuthResponse> getCurrentUser(Authentication authentication) {
        try {
            Long userId = Long.parseLong(authentication.getName());
            User user = authService.getUserById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            AuthResponse response = new AuthResponse();
            response.setSuccess(true);
            response.setMessage("User retrieved successfully");
            response.setUserId(user.getUserId());
            response.setEmail(user.getEmail());
            response.setFullName(user.getFullName());
            response.setRole(user.getRole().toString());

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            AuthResponse response = new AuthResponse();
            response.setSuccess(false);
            response.setMessage(e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
    }

    /**
     * GET /auth/user/{userId}
     * Fetch user details by ID (used by instructor students page)
     */
    @GetMapping("/user/{userId}")
    @Operation(summary = "Get user by ID", description = "Retrieve user details by user ID (authenticated users only)")
    public ResponseEntity<Map<String, Object>> getUserById(@PathVariable Long userId) {
        try {
            User user = authService.getUserById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "User retrieved successfully",
                    "data", toSummaryDTO(user)
            ));
        } catch (RuntimeException e) {
            Map<String, Object> errorResponse = new java.util.HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            errorResponse.put("data", null);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
    }

    /**
     * PUT /auth/profile
     * Update fullName, bio, mobile
     */
    @PutMapping("/profile")
    @Operation(summary = "Update profile", description = "Update user's fullName, bio, and mobile number")
    public ResponseEntity<AuthResponse> updateProfile(@RequestBody UpdateProfileRequest request) {
        try {
            User updatedUser = authService.updateProfile(
                    request.getUserId(),
                    request.getFullName(),
                    request.getBio(),
                    request.getMobile()
            );

            AuthResponse response = new AuthResponse();
            response.setSuccess(true);
            response.setMessage("Profile updated successfully");
            response.setUserId(updatedUser.getUserId());
            response.setEmail(updatedUser.getEmail());
            response.setFullName(updatedUser.getFullName());
            response.setRole(updatedUser.getRole().toString());

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            AuthResponse response = new AuthResponse();
            response.setSuccess(false);
            response.setMessage(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    /**
     * POST /auth/change-password
     * Change user password (validates old password first)
     */
    @PostMapping("/change-password")
    @Operation(summary = "Change password", description = "Change user password by validating the old password first")
    public ResponseEntity<AuthResponse> changePassword(@RequestBody ChangePasswordRequest request) {
        try {
            authService.changePassword(
                    request.getUserId(),
                    request.getOldPassword(),
                    request.getNewPassword()
            );

            AuthResponse response = new AuthResponse();
            response.setSuccess(true);
            response.setMessage("Password changed successfully");

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            AuthResponse response = new AuthResponse();
            response.setSuccess(false);
            response.setMessage(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // ADMIN ENDPOINTS (Backend Gap 2)
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * GET /auth/users
     * List all users (ADMIN only) — never exposes passwordHash
     */
    @GetMapping("/users")
    @Operation(summary = "Get all users", description = "Retrieve all users (ADMIN only)")
    public ResponseEntity<Map<String, Object>> getAllUsers() {
        List<UserSummaryDTO> users = userRepository.findAll()
                .stream()
                .map(this::toSummaryDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Users retrieved successfully",
                "data", users
        ));
    }

    /**
     * GET /auth/users/role/{role}
     * Filter users by role (ADMIN only)
     */
    @GetMapping("/users/role/{role}")
    @Operation(summary = "Get users by role", description = "Retrieve all users with a specific role (ADMIN only)")
    public ResponseEntity<Map<String, Object>> getUsersByRole(@PathVariable String role) {
        try {
            UserRole userRole = UserRole.valueOf(role.toUpperCase());
            List<UserSummaryDTO> users = userRepository.findAllByRole(userRole)
                    .stream()
                    .map(this::toSummaryDTO)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Users retrieved successfully",
                    "data", users
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Invalid role",
                    "data", List.of()
            ));
        }
    }

    /**
     * DELETE /auth/users/{userId}
     * Delete user (ADMIN only)
     */
    @DeleteMapping("/users/{userId}")
    @Operation(summary = "Delete user", description = "Delete a user by ID (ADMIN only)")
    public ResponseEntity<AuthResponse> deleteUser(@PathVariable Long userId) {
        try {
            authService.deleteUser(userId);

            AuthResponse response = new AuthResponse();
            response.setSuccess(true);
            response.setMessage("User deleted successfully");
            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            AuthResponse response = new AuthResponse();
            response.setSuccess(false);
            response.setMessage(e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }
}
