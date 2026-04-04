package com.edulearn.auth.controller;

import com.edulearn.auth.dto.AuthResponse;
import com.edulearn.auth.dto.LoginRequest;
import com.edulearn.auth.dto.RegisterRequest;
import com.edulearn.auth.entity.User;
import com.edulearn.auth.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "*")
@Tag(name = "Authentication", description = "Authentication and Authorization API endpoints")
public class AuthController {

    @Autowired
    private AuthService authService;

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
            // Call service to register user
            User user = authService.register(
                    request.getEmail(),
                    request.getFullName(),
                    request.getPassword(),
                    request.getRole()
            );

            // Build success response
            AuthResponse response = new AuthResponse();
            response.setSuccess(true);
            response.setMessage("User registered successfully");
            response.setUserId(user.getUserId());
            response.setEmail(user.getEmail());
            response.setFullName(user.getFullName());
            response.setRole(user.getRole().toString());

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (RuntimeException e) {
            // Build error response
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
            // Call service to login
            String token = authService.login(request.getEmail(), request.getPassword());

            // Get user details
            User user = authService.getUserByEmail(request.getEmail()).orElseThrow();

            // Build success response
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
            // Build error response
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
}
