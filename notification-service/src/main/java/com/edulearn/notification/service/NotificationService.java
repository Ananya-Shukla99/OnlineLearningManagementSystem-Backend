package com.edulearn.notification.service;

import com.edulearn.notification.entity.Notification;
import com.edulearn.notification.event.EnrollmentEvent;
import com.edulearn.notification.event.PaymentEvent;
import com.edulearn.notification.event.QuizResultEvent;
import com.edulearn.notification.event.CertificateEvent;
import java.util.List;

public interface NotificationService {

    // Core notification operations
    Notification sendNotification(Long userId, String type, String title,
                                  String message, Long relatedEntityId,
                                  String relatedEntityType);

    List<Notification> getNotificationsByUser(Long userId);

    List<Notification> getUnreadNotifications(Long userId);

    int getUnreadCount(Long userId);

    void markAsRead(Long notificationId);

    void markAllAsRead(Long userId);

    void deleteNotification(Long notificationId);

    List<Notification> getNotificationsByType(Long userId, String type);

    // Event handlers - these listen for published events
    void handleEnrollmentEvent(EnrollmentEvent event);

    void handlePaymentEvent(PaymentEvent event);

    void handleQuizResultEvent(QuizResultEvent event);

    void handleCertificateEvent(CertificateEvent event);
}

