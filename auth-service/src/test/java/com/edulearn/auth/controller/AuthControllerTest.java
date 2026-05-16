package com.edulearn.auth.controller;

import com.edulearn.auth.dto.LoginRequest;
import com.edulearn.auth.dto.RegisterRequest;
import com.edulearn.auth.dto.AuthResponse;
import com.edulearn.auth.entity.User;
import com.edulearn.auth.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.security.core.Authentication;
import static org.mockito.Mockito.mock;


import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.springframework.test.context.TestPropertySource;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
@TestPropertySource(properties = {
        "spring.cloud.discovery.enabled=false",
        "spring.cloud.config.enabled=false",
        "eureka.client.enabled=false"
})
@DisplayName("Auth Controller Tests")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;
    
    @MockitoBean
    private com.edulearn.auth.service.OtpService otpService;
    
    @MockitoBean
    private com.edulearn.auth.repository.UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("POST /register - Success")
    void testRegister() throws Exception {
        User user = new User();
        user.setEmail("test@example.com");
        user.setUserId(1L);
        user.setRole(com.edulearn.auth.entity.UserRole.STUDENT);

        when(otpService.verifyOtp(anyString(), anyString())).thenReturn(true);
        when(authService.register(anyString(), anyString(), anyString(), anyString())).thenReturn(user);

        RegisterRequest request = new RegisterRequest();
        request.setEmail("test@example.com");
        request.setPassword("password");
        request.setFullName("Test User");
        request.setRole("STUDENT");
        request.setOtp("123456");

        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("POST /register - Invalid OTP")
    void testRegisterInvalidOtp() throws Exception {
        when(otpService.verifyOtp(anyString(), anyString())).thenReturn(false);

        RegisterRequest request = new RegisterRequest();
        request.setEmail("test@example.com");
        request.setOtp("wrong");

        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid or expired OTP"));
    }

    @Test
    @DisplayName("POST /send-otp - Success")
    void testSendOtpSuccess() throws Exception {
        when(userRepository.findByEmail(anyString())).thenReturn(java.util.Optional.empty());

        mockMvc.perform(post("/auth/send-otp?email=test@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("POST /send-otp - Email already exists")
    void testSendOtpEmailExists() throws Exception {
        when(userRepository.findByEmail(anyString())).thenReturn(java.util.Optional.of(new User()));

        mockMvc.perform(post("/auth/send-otp?email=test@example.com"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Email is already registered"));
    }

    @Test
    @DisplayName("POST /send-otp - Internal Server Error")
    void testSendOtpFailure() throws Exception {
        when(userRepository.findByEmail(anyString())).thenThrow(new RuntimeException("DB down"));

        mockMvc.perform(post("/auth/send-otp?email=test@example.com"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("POST /login - Success")
    void testLogin() throws Exception {
        AuthResponse response = new AuthResponse();
        response.setToken("token");
        response.setSuccess(true);
        when(authService.login(anyString(), anyString())).thenReturn("token");

        // Mock getting user by email for the controller logic
        User user = new User();
        user.setUserId(1L);
        user.setEmail("test@example.com");
        user.setRole(com.edulearn.auth.entity.UserRole.STUDENT);
        when(authService.getUserByEmail(anyString())).thenReturn(java.util.Optional.of(user));

        LoginRequest request = new LoginRequest();
        request.setEmail("test@example.com");
        request.setPassword("password");

        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.token").value("token"));
    }

    @Test
    @DisplayName("POST /validate - Success")
    void testValidate() throws Exception {
        when(authService.validateToken(anyString())).thenReturn("1");

        mockMvc.perform(get("/auth/validate?token=valid-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.userId").value(1));
    }

    @Test
    @DisplayName("POST /refresh - Success")
    void testRefresh() throws Exception {
        when(authService.refreshToken(anyString())).thenReturn("new-token");

        mockMvc.perform(post("/auth/refresh?token=old-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.token").value("new-token"));
    }

    @Test
    @DisplayName("GET /me - Success")
    void testMeSuccess() throws Exception {
        User user = new User();
        user.setUserId(1L);
        user.setRole(com.edulearn.auth.entity.UserRole.STUDENT);
        
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("1");
        when(authService.getUserById(1L)).thenReturn(java.util.Optional.of(user));

        mockMvc.perform(get("/auth/me").principal(auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("GET /me - User not found")
    void testMeUserNotFound() throws Exception {
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("1");
        when(authService.getUserById(1L)).thenReturn(java.util.Optional.empty());

        mockMvc.perform(get("/auth/me").principal(auth))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("GET /me - Unauthorized without principal")
    void testMeUnauthorized() throws Exception {
        mockMvc.perform(get("/auth/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("PUT /profile - Success")
    void testUpdateProfile() throws Exception {
        User user = new User();
        user.setUserId(1L);
        user.setEmail("test@example.com");
        user.setRole(com.edulearn.auth.entity.UserRole.STUDENT);
        when(authService.updateProfile(anyLong(), anyString(), any(), any(), any(), any())).thenReturn(user);

        // Controller reads userId, fullName, bio, mobile from UpdateProfileRequest
        String body = "{\"userId\":1, \"fullName\":\"New Name\", \"bio\":\"Bio\", \"mobile\":\"1234567890\"}";
        mockMvc.perform(put("/auth/profile")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("PUT /profile - Failure")
    void testUpdateProfileFailure() throws Exception {
        when(authService.updateProfile(anyLong(), anyString(), any(), any(), any(), any())).thenThrow(new RuntimeException("Update failed"));
        mockMvc.perform(put("/auth/profile")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"userId\":1}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("DELETE /users/{id} - Success")
    void testDeleteUser() throws Exception {
        mockMvc.perform(delete("/auth/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("DELETE /users/{id} - Not Found")
    void testDeleteUserNotFound() throws Exception {
        doThrow(new RuntimeException("Not found")).when(authService).deleteUser(999L);
        mockMvc.perform(delete("/auth/users/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /change-password - Success")
    void testChangePassword() throws Exception {
        String body = "{\"userId\":1, \"oldPassword\":\"old\", \"newPassword\":\"new\"}";
        mockMvc.perform(post("/auth/change-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("GET /user/{id} - Success")
    void testGetUserById() throws Exception {
        User user = new User();
        user.setUserId(1L);
        user.setEmail("test@example.com");
        user.setFullName("Test User");
        user.setRole(com.edulearn.auth.entity.UserRole.STUDENT);
        // getUserById in controller calls authService.getUserById
        when(authService.getUserById(1L)).thenReturn(java.util.Optional.of(user));

        mockMvc.perform(get("/auth/user/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("GET /users - Success")
    void testGetAllUsers() throws Exception {
        when(userRepository.findAll()).thenReturn(java.util.List.of());
        mockMvc.perform(get("/auth/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("POST /register - User already exists")
    void testRegisterUserExists() throws Exception {
        when(authService.register(any(), any(), any(), any())).thenThrow(new RuntimeException("User exists"));
        
        RegisterRequest request = new RegisterRequest();
        request.setEmail("exists@test.com");
        request.setFullName("Name");
        request.setPassword("pass");
        request.setRole("STUDENT");

        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("POST /login - User not found")
    void testLoginNotFound() throws Exception {
        when(authService.login(any(), any())).thenThrow(new RuntimeException("User not found"));
        
        LoginRequest request = new LoginRequest();
        request.setEmail("none@test.com");
        request.setPassword("pass");

        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /users/role/{role} - Invalid role")
    void testGetUsersByRoleInvalid() throws Exception {
        mockMvc.perform(get("/auth/users/role/INVALID"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /user/{id} - Not Found")
    void testGetUserByIdNotFound() throws Exception {
        when(authService.getUserById(anyLong())).thenReturn(java.util.Optional.empty());
        mockMvc.perform(get("/auth/user/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /users/role/{role} - Success")
    void testGetUsersByRoleSuccess() throws Exception {
        when(userRepository.findAllByRole(any())).thenReturn(java.util.List.of());
        mockMvc.perform(get("/auth/users/role/STUDENT"))
                .andExpect(status().isOk());
    }
}
