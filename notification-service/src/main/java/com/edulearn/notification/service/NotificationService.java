package com.edulearn.notification.service;

import com.edulearn.notification.entity.Notification;
import com.edulearn.notification.event.EnrollmentEvent;
import com.edulearn.notification.event.PaymentEvent;
import com.edulearn.notification.event.QuizResultEvent;
import com.edulearn.notification.event.CertificateEvent;
import java.util.List;

public interface NotificationService {

    // Core notification operations
    Notification sendNotification(Integer userId, String type, String title,
                                  String message, Integer relatedEntityId,
                                  String relatedEntityType);

    List<Notification> getNotificationsByUser(Integer userId);

    List<Notification> getUnreadNotifications(Integer userId);

    int getUnreadCount(Integer userId);

    void markAsRead(Integer notificationId);

    void markAllAsRead(Integer userId);

    void deleteNotification(Integer notificationId);

    List<Notification> getNotificationsByType(Integer userId, String type);

    // Event handlers - these listen for published events
    void handleEnrollmentEvent(EnrollmentEvent event);

    void handlePaymentEvent(PaymentEvent event);

    void handleQuizResultEvent(QuizResultEvent event);

    void handleCertificateEvent(CertificateEvent event);
}

