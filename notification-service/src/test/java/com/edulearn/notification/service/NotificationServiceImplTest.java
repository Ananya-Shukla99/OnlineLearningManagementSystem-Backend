package com.edulearn.notification.service;

import com.edulearn.notification.entity.Notification;
import com.edulearn.notification.event.EnrollmentEvent;
import com.edulearn.notification.event.PaymentEvent;
import com.edulearn.notification.event.QuizResultEvent;
import com.edulearn.notification.event.CertificateEvent;
import com.edulearn.notification.repository.NotificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class NotificationServiceImplTest {

    @Mock
    private NotificationRepository notificationRepository;

    @InjectMocks
    private NotificationServiceImpl notificationService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testSendNotification() {
        Notification notification = new Notification();
        notification.setNotificationId(1L);
        notification.setUserId(101L);
        notification.setType("ENROLLMENT");
        notification.setTitle("Test");
        notification.setMessage("Test message");
        notification.setIsRead(false);

        when(notificationRepository.save(any(Notification.class))).thenReturn(notification);

        Notification result = notificationService.sendNotification(
            101L, "ENROLLMENT", "Test", "Test message", 1L, "COURSE"
        );

        assertNotNull(result);
        assertEquals("ENROLLMENT", result.getType());
        assertFalse(result.getIsRead());
        verify(notificationRepository, times(1)).save(any(Notification.class));
    }

    @Test
    public void testGetNotificationsByUser() {
        Notification notif = new Notification();
        notif.setNotificationId(1L);
        notif.setUserId(101L);
        notif.setType("ENROLLMENT");

        when(notificationRepository.findByUserIdOrderByCreatedAtDesc(101L))
            .thenReturn(Arrays.asList(notif));

        List<Notification> result = notificationService.getNotificationsByUser(101L);

        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        verify(notificationRepository, times(1)).findByUserIdOrderByCreatedAtDesc(101L);
    }

    @Test
    public void testGetUnreadNotifications() {
        Notification notif = new Notification();
        notif.setNotificationId(1L);
        notif.setUserId(101L);
        notif.setIsRead(false);

        when(notificationRepository.findByUserIdAndIsRead(101L, false))
            .thenReturn(Arrays.asList(notif));

        List<Notification> result = notificationService.getUnreadNotifications(101L);

        assertFalse(result.isEmpty());
        assertFalse(result.get(0).getIsRead());
        verify(notificationRepository, times(1)).findByUserIdAndIsRead(101L, false);
    }

    @Test
    public void testGetUnreadCount() {
        when(notificationRepository.countByUserIdAndIsRead(101L, false)).thenReturn(3);

        int count = notificationService.getUnreadCount(101L);

        assertEquals(3, count);
        verify(notificationRepository, times(1)).countByUserIdAndIsRead(101L, false);
    }

    @Test
    public void testMarkAsRead() {
        Notification notif = new Notification();
        notif.setNotificationId(1L);
        notif.setIsRead(false);

        when(notificationRepository.findById(1L)).thenReturn(Optional.of(notif));
        when(notificationRepository.save(any(Notification.class))).thenReturn(notif);

        notificationService.markAsRead(1L);

        verify(notificationRepository, times(1)).findById(1L);
        verify(notificationRepository, times(1)).save(any(Notification.class));
    }

    @Test
    public void testMarkAllAsRead() {
        Notification notif1 = new Notification();
        notif1.setNotificationId(1L);
        notif1.setIsRead(false);

        Notification notif2 = new Notification();
        notif2.setNotificationId(2L);
        notif2.setIsRead(false);

        when(notificationRepository.findByUserIdAndIsRead(101L, false))
            .thenReturn(Arrays.asList(notif1, notif2));
        when(notificationRepository.saveAll(any())).thenReturn(Arrays.asList(notif1, notif2));

        notificationService.markAllAsRead(101L);

        verify(notificationRepository, times(1)).findByUserIdAndIsRead(101L, false);
        verify(notificationRepository, times(1)).saveAll(any());
    }

    @Test
    public void testDeleteNotification() {
        notificationService.deleteNotification(1L);

        verify(notificationRepository, times(1)).deleteById(1L);
    }

    @Test
    public void testGetNotificationsByType() {
        Notification notif = new Notification();
        notif.setType("PAYMENT");

        when(notificationRepository.findByUserIdAndType(101L, "PAYMENT"))
            .thenReturn(Arrays.asList(notif));

        List<Notification> result = notificationService.getNotificationsByType(101L, "PAYMENT");

        assertFalse(result.isEmpty());
        assertEquals("PAYMENT", result.get(0).getType());
    }

    @Test
    public void testHandleEnrollmentEvent() {
        EnrollmentEvent event = new EnrollmentEvent(this, 101L, 5L, "Java Course");

        when(notificationRepository.save(any(Notification.class)))
            .thenAnswer(invocation -> {
                Notification n = invocation.getArgument(0);
                n.setNotificationId(1L);
                return n;
            });

        notificationService.handleEnrollmentEvent(event);

        verify(notificationRepository, times(1)).save(any(Notification.class));
    }

    @Test
    public void testHandlePaymentEvent() {
        PaymentEvent event = new PaymentEvent(this, 101L, 500.0, "Java Course");

        when(notificationRepository.save(any(Notification.class)))
            .thenAnswer(invocation -> {
                Notification n = invocation.getArgument(0);
                n.setNotificationId(1L);
                return n;
            });

        notificationService.handlePaymentEvent(event);

        verify(notificationRepository, times(1)).save(any(Notification.class));
    }

    @Test
    public void testHandleQuizResultEventPassed() {
        QuizResultEvent event = new QuizResultEvent(this, 101L, "Java Basics", 85, true);

        when(notificationRepository.save(any(Notification.class)))
            .thenAnswer(invocation -> {
                Notification n = invocation.getArgument(0);
                n.setNotificationId(1L);
                return n;
            });

        notificationService.handleQuizResultEvent(event);

        verify(notificationRepository, times(1)).save(any(Notification.class));
    }

    @Test
    public void testHandleQuizResultEventFailed() {
        QuizResultEvent event = new QuizResultEvent(this, 101L, "Java Basics", 45, false);

        when(notificationRepository.save(any(Notification.class)))
            .thenAnswer(invocation -> {
                Notification n = invocation.getArgument(0);
                n.setNotificationId(1L);
                return n;
            });

        notificationService.handleQuizResultEvent(event);

        verify(notificationRepository, times(1)).save(any(Notification.class));
    }

    @Test
    public void testHandleCertificateEvent() {
        CertificateEvent event = new CertificateEvent(this, 101L, "Java Course", "EL-2026-AB-XYZ123");

        when(notificationRepository.save(any(Notification.class)))
            .thenAnswer(invocation -> {
                Notification n = invocation.getArgument(0);
                n.setNotificationId(1L);
                return n;
            });

        notificationService.handleCertificateEvent(event);

        verify(notificationRepository, times(1)).save(any(Notification.class));
    }
}

