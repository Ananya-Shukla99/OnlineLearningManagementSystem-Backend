package com.edulearn.notification.controller;

import com.edulearn.notification.entity.Notification;
import com.edulearn.notification.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import java.util.Arrays;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class NotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private NotificationService notificationService;


    private Notification testNotification;

    @BeforeEach
    public void setUp() {
        testNotification = new Notification();
        testNotification.setNotificationId(1);
        testNotification.setUserId(101);
        testNotification.setType("ENROLLMENT");
        testNotification.setTitle("Enrolled successfully!");
        testNotification.setMessage("You have enrolled in Advanced Java. Start learning now!");
        testNotification.setIsRead(false);
    }

    @Test
    @WithMockUser(roles = "STUDENT")
    public void testGetNotificationsByUser() throws Exception {
        when(notificationService.getNotificationsByUser(101))
            .thenReturn(Arrays.asList(testNotification));

        mockMvc.perform(get("/api/notifications/user/101"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].type").value("ENROLLMENT"))
                .andExpect(jsonPath("$[0].title").value("Enrolled successfully!"));

        verify(notificationService, times(1)).getNotificationsByUser(101);
    }

    @Test
    @WithMockUser(roles = "STUDENT")
    public void testGetUnreadCount() throws Exception {
        when(notificationService.getUnreadCount(101)).thenReturn(3);

        mockMvc.perform(get("/api/notifications/unread-count/101"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(3));

        verify(notificationService, times(1)).getUnreadCount(101);
    }

    @Test
    @WithMockUser(roles = "STUDENT")
    public void testGetUnreadCount_Zero() throws Exception {
        when(notificationService.getUnreadCount(101)).thenReturn(0);

        mockMvc.perform(get("/api/notifications/unread-count/101"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(0));
    }

    @Test
    @WithMockUser(roles = "STUDENT")
    public void testGetUnreadNotifications() throws Exception {
        testNotification.setIsRead(false);
        when(notificationService.getUnreadNotifications(101))
            .thenReturn(Arrays.asList(testNotification));

        mockMvc.perform(get("/api/notifications/unread/101"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].isRead").value(false));

        verify(notificationService, times(1)).getUnreadNotifications(101);
    }

    @Test
    @WithMockUser(roles = "STUDENT")
    public void testMarkAsRead() throws Exception {
        doNothing().when(notificationService).markAsRead(1);

        mockMvc.perform(put("/api/notifications/1/read")
                .with(csrf()))
                .andExpect(status().isOk());

        verify(notificationService, times(1)).markAsRead(1);
    }

    @Test
    @WithMockUser(roles = "STUDENT")
    public void testMarkAllAsRead() throws Exception {
        doNothing().when(notificationService).markAllAsRead(101);

        mockMvc.perform(put("/api/notifications/read-all/101")
                .with(csrf()))
                .andExpect(status().isOk());

        verify(notificationService, times(1)).markAllAsRead(101);
    }

    @Test
    @WithMockUser(roles = "STUDENT")
    public void testDeleteNotification() throws Exception {
        doNothing().when(notificationService).deleteNotification(1);

        mockMvc.perform(delete("/api/notifications/1")
                .with(csrf()))
                .andExpect(status().isOk());

        verify(notificationService, times(1)).deleteNotification(1);
    }

    @Test
    @WithMockUser(roles = "STUDENT")
    public void testGetNotificationsByType() throws Exception {
        Notification paymentNotif = new Notification();
        paymentNotif.setNotificationId(2);
        paymentNotif.setUserId(101);
        paymentNotif.setType("PAYMENT");
        paymentNotif.setTitle("Payment successful");

        when(notificationService.getNotificationsByType(101, "PAYMENT"))
            .thenReturn(Arrays.asList(paymentNotif));

        mockMvc.perform(get("/api/notifications/type?userId=101&type=PAYMENT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].type").value("PAYMENT"));

        verify(notificationService, times(1)).getNotificationsByType(101, "PAYMENT");
    }

    @Test
    @WithMockUser(roles = "STUDENT")
    public void testGetNotificationsByType_QuizResult() throws Exception {
        Notification quizNotif = new Notification();
        quizNotif.setNotificationId(3);
        quizNotif.setUserId(101);
        quizNotif.setType("QUIZ_RESULT");
        quizNotif.setTitle("Quiz result: Passed (85%)");

        when(notificationService.getNotificationsByType(101, "QUIZ_RESULT"))
            .thenReturn(Arrays.asList(quizNotif));

        mockMvc.perform(get("/api/notifications/type?userId=101&type=QUIZ_RESULT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].type").value("QUIZ_RESULT"));
    }

    @Test
    @WithMockUser(roles = "STUDENT")
    public void testGetNotificationsByType_Certificate() throws Exception {
        Notification certNotif = new Notification();
        certNotif.setNotificationId(4);
        certNotif.setUserId(101);
        certNotif.setType("CERTIFICATE");
        certNotif.setTitle("Certificate earned!");

        when(notificationService.getNotificationsByType(101, "CERTIFICATE"))
            .thenReturn(Arrays.asList(certNotif));

        mockMvc.perform(get("/api/notifications/type?userId=101&type=CERTIFICATE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].type").value("CERTIFICATE"));
    }

    @Test
    public void testGetNotificationsByUser_Unauthenticated() throws Exception {
        mockMvc.perform(get("/api/notifications/user/101"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "STUDENT")
    public void testGetUnreadCount_DifferentUser() throws Exception {
        when(notificationService.getUnreadCount(102)).thenReturn(1);

        mockMvc.perform(get("/api/notifications/unread-count/102"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(1));
    }
}

