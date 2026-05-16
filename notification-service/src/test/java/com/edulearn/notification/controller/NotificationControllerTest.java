package com.edulearn.notification.controller;

import com.edulearn.notification.entity.Notification;
import com.edulearn.notification.service.NotificationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.springframework.test.context.TestPropertySource;

@WebMvcTest(NotificationController.class)
@AutoConfigureMockMvc(addFilters = false)
@TestPropertySource(properties = {
    "spring.cloud.discovery.enabled=false",
    "spring.cloud.config.enabled=false",
    "eureka.client.enabled=false"
})
@DisplayName("Notification Controller Tests")
class NotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private NotificationService notificationService;

    @MockBean
    private com.edulearn.notification.config.JwtAuthFilter jwtAuthFilter;

    @MockBean
    private com.edulearn.notification.config.JwtUtil jwtUtil;

    @MockBean
    private org.springframework.amqp.rabbit.connection.ConnectionFactory connectionFactory;

    @Autowired
    private ObjectMapper objectMapper;

    private Notification testNotification;

    @BeforeEach
    void setUp() {
        testNotification = new Notification();
        testNotification.setNotificationId(1L);
        testNotification.setUserId(101L);
        testNotification.setType("TEST");
        testNotification.setTitle("Hello");
        testNotification.setMessage("World");
        testNotification.setIsRead(false);
        testNotification.setCreatedAt(LocalDateTime.now());
    }

    @Test
    @DisplayName("GET /user/{userId} - Success")
    void testGetByUser() throws Exception {
        when(notificationService.getNotificationsByUser(101L)).thenReturn(List.of(testNotification));

        mockMvc.perform(get("/api/notifications/user/101"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].userId").value(101L));
    }

    @Test
    @DisplayName("GET /unread/{userId} - Success")
    void testGetUnread() throws Exception {
        when(notificationService.getUnreadNotifications(101L)).thenReturn(List.of(testNotification));

        mockMvc.perform(get("/api/notifications/unread/101"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].userId").value(101L));
    }

    @Test
    @DisplayName("GET /unread-count/{userId} - Success")
    void testGetUnreadCount() throws Exception {
        when(notificationService.getUnreadCount(101L)).thenReturn(5);

        mockMvc.perform(get("/api/notifications/unread-count/101"))
                .andExpect(status().isOk())
                .andExpect(content().string("5"));
    }

    @Test
    @DisplayName("PUT /{notificationId}/read - Success")
    void testMarkAsRead() throws Exception {
        doNothing().when(notificationService).markAsRead(1L);

        mockMvc.perform(put("/api/notifications/1/read"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("PUT /read-all/{userId} - Success")
    void testMarkAllAsRead() throws Exception {
        doNothing().when(notificationService).markAllAsRead(101L);

        mockMvc.perform(put("/api/notifications/read-all/101"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("DELETE /{notificationId} - Success")
    void testDelete() throws Exception {
        doNothing().when(notificationService).deleteNotification(1L);

        mockMvc.perform(delete("/api/notifications/1"))
                .andExpect(status().isOk());
    }
}
