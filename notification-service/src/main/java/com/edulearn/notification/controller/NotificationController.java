package com.edulearn.notification.controller;

import com.edulearn.notification.entity.Notification;
import com.edulearn.notification.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@CrossOrigin(origins = "http://localhost:4200")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    /**
     * Get all notifications for a user (newest first)
     * Angular uses this to populate the notification dropdown
     */
    @GetMapping("/user/{userId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<Notification>> getNotificationsByUser(@PathVariable Integer userId) {
        List<Notification> notifications = notificationService.getNotificationsByUser(userId);
        return ResponseEntity.ok(notifications);
    }

    /**
     * Get count of UNREAD notifications for a user
     * Angular polls this every 30 seconds to update the bell badge
     */
    @GetMapping("/unread-count/{userId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Integer> getUnreadCount(@PathVariable Integer userId) {
        int count = notificationService.getUnreadCount(userId);
        return ResponseEntity.ok(count);
    }

    /**
     * Get only unread notifications for a user
     */
    @GetMapping("/unread/{userId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<Notification>> getUnreadNotifications(@PathVariable Integer userId) {
        List<Notification> notifications = notificationService.getUnreadNotifications(userId);
        return ResponseEntity.ok(notifications);
    }

    /**
     * Mark a single notification as read
     */
    @PutMapping("/{notificationId}/read")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> markAsRead(@PathVariable Integer notificationId) {
        try {
            notificationService.markAsRead(notificationId);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Mark all notifications for a user as read
     */
    @PutMapping("/read-all/{userId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> markAllAsRead(@PathVariable Integer userId) {
        notificationService.markAllAsRead(userId);
        return ResponseEntity.ok().build();
    }

    /**
     * Delete a single notification
     */
    @DeleteMapping("/{notificationId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> deleteNotification(@PathVariable Integer notificationId) {
        try {
            notificationService.deleteNotification(notificationId);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Get notifications filtered by type (e.g., ENROLLMENT, PAYMENT, QUIZ_RESULT)
     */
    @GetMapping("/type")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<Notification>> getNotificationsByType(
            @RequestParam Integer userId,
            @RequestParam String type) {
        List<Notification> notifications = notificationService.getNotificationsByType(userId, type);
        return ResponseEntity.ok(notifications);
    }
}

