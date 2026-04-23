package com.edulearn.notification.service;

import com.edulearn.notification.entity.Notification;
import com.edulearn.notification.repository.NotificationRepository;
import com.edulearn.notification.event.EnrollmentEvent;
import com.edulearn.notification.event.PaymentEvent;
import com.edulearn.notification.event.QuizResultEvent;
import com.edulearn.notification.event.CertificateEvent;
import com.edulearn.notification.dto.NotificationDto;
import com.edulearn.notification.config.RabbitMQConfig;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class NotificationServiceImpl implements NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    @RabbitListener(queues = RabbitMQConfig.QUEUE)
    public void handleNotification(NotificationDto dto) {
        System.out.println("Received Notification from RabbitMQ: " + dto);
        sendNotification(
                dto.getUserId(),
                dto.getType(),
                dto.getTitle(),
                dto.getMessage(),
                dto.getRelatedEntityId(),
                dto.getRelatedEntityType());
    }

    @Override
    public Notification sendNotification(Long userId, String type, String title,
            String message, Long relatedEntityId,
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
    public List<Notification> getNotificationsByUser(Long userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    @Override
    public List<Notification> getUnreadNotifications(Long userId) {
        return notificationRepository.findByUserIdAndIsRead(userId, false);
    }

    @Override
    public int getUnreadCount(Long userId) {
        return notificationRepository.countByUserIdAndIsRead(userId, false);
    }

    @Override
    public void markAsRead(Long notificationId) {
        Notification n = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found"));
        n.setIsRead(true);
        notificationRepository.save(n);
    }

    @Override
    public void markAllAsRead(Long userId) {
        List<Notification> unread = notificationRepository.findByUserIdAndIsRead(userId, false);
        unread.forEach(n -> n.setIsRead(true));
        notificationRepository.saveAll(unread);
    }

    @Override
    public void deleteNotification(Long notificationId) {
        notificationRepository.deleteById(notificationId);
    }

    @Override
    public List<Notification> getNotificationsByType(Long userId, String type) {
        return notificationRepository.findByUserIdAndType(userId, type);
    }

    // ==================== EVENT LISTENERS (DEFERRED) ====================
    // NOTE: These listeners are currently INACTIVE.
    // Cross-service Spring events do not work across microservice boundaries.
    // Full migration to RabbitMQ/Kafka is deferred.
    // ====================================================================

    @EventListener
    @Override
    public void handleEnrollmentEvent(EnrollmentEvent event) {
        sendNotification(
                event.getStudentId(),
                "ENROLLMENT",
                "Enrolled successfully!",
                "You have enrolled in " + event.getCourseTitle() + ". Start learning now!",
                event.getCourseId(),
                "COURSE");
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
                null);
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
                null);
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
                "CERTIFICATE");
    }
}
