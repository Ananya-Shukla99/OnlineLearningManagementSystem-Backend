package com.edulearn.notification.service;

import com.edulearn.notification.entity.Notification;
import com.edulearn.notification.event.EnrollmentEvent;
import com.edulearn.notification.event.PaymentEvent;
import com.edulearn.notification.event.QuizResultEvent;
import com.edulearn.notification.event.CertificateEvent;
import com.edulearn.notification.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class NotificationServiceImpl implements NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    @Override
    public Notification sendNotification(Integer userId, String type, String title,
                                        String message, Integer relatedEntityId,
                                        String relatedEntityType) {
        Notification n = new Notification();
        n.setUserId(userId);
        n.setType(type);
        n.setTitle(title);
        n.setMessage(message);
        n.setIsRead(false);
        n.setRelatedEntityId(relatedEntityId);
        n.setRelatedEntityType(relatedEntityType);
        return notificationRepository.save(n);
    }

    @Override
    public List<Notification> getNotificationsByUser(Integer userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    @Override
    public List<Notification> getUnreadNotifications(Integer userId) {
        return notificationRepository.findByUserIdAndIsRead(userId, false);
    }

    @Override
    public int getUnreadCount(Integer userId) {
        return notificationRepository.countByUserIdAndIsRead(userId, false);
    }

    @Override
    public void markAsRead(Integer notificationId) {
        Notification n = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found"));
        n.setIsRead(true);
        notificationRepository.save(n);
    }

    @Override
    public void markAllAsRead(Integer userId) {
        List<Notification> unread = notificationRepository.findByUserIdAndIsRead(userId, false);
        unread.forEach(n -> n.setIsRead(true));
        notificationRepository.saveAll(unread);
    }

    @Override
    public void deleteNotification(Integer notificationId) {
        notificationRepository.deleteById(notificationId);
    }

    @Override
    public List<Notification> getNotificationsByType(Integer userId, String type) {
        return notificationRepository.findByUserIdAndType(userId, type);
    }

    // ==================== EVENT LISTENERS ====================
    // These methods automatically fire when events are published anywhere in the app

    @EventListener
    @Override
    public void handleEnrollmentEvent(EnrollmentEvent event) {
        sendNotification(
            event.getStudentId(),
            "ENROLLMENT",
            "Enrolled successfully!",
            "You have enrolled in " + event.getCourseTitle() + ". Start learning now!",
            event.getCourseId(),
            "COURSE"
        );
    }

    @EventListener
    @Override
    public void handlePaymentEvent(PaymentEvent event) {
        sendNotification(
            event.getStudentId(),
            "PAYMENT",
            "Payment successful",
            "Payment of ₹" + event.getAmount() + " for " + event.getCourseTitle() + " was successful.",
            null,
            null
        );
    }

    @EventListener
    @Override
    public void handleQuizResultEvent(QuizResultEvent event) {
        String resultText = event.isPassed() ? "Passed" : "Failed";
        sendNotification(
            event.getStudentId(),
            "QUIZ_RESULT",
            "Quiz result: " + resultText + " (" + event.getScore() + "%)",
            "You scored " + event.getScore() + "% on '" + event.getQuizTitle() + "'. " +
            (event.isPassed() ? "Congratulations!" : "Keep practicing!"),
            null,
            null
        );
    }

    @EventListener
    @Override
    public void handleCertificateEvent(CertificateEvent event) {
        sendNotification(
            event.getStudentId(),
            "CERTIFICATE",
            "Certificate earned!",
            "Congratulations! Your certificate for '" + event.getCourseName() + "' has been issued. " +
            "Verification code: " + event.getVerificationCode(),
            null,
            "CERTIFICATE"
        );
    }
}

