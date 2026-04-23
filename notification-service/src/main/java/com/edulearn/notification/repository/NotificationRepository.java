package com.edulearn.notification.repository;

import com.edulearn.notification.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByUserId(Long userId);

    List<Notification> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<Notification> findByUserIdAndIsRead(Long userId, Boolean isRead);

    int countByUserIdAndIsRead(Long userId, Boolean isRead);

    List<Notification> findByType(String type);

    List<Notification> findByUserIdAndType(Long userId, String type);
}

