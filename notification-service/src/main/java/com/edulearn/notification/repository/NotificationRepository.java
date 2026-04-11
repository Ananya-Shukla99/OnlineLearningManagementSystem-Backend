package com.edulearn.notification.repository;

import com.edulearn.notification.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Integer> {
    List<Notification> findByUserId(Integer userId);

    List<Notification> findByUserIdOrderByCreatedAtDesc(Integer userId);

    List<Notification> findByUserIdAndIsRead(Integer userId, Boolean isRead);

    int countByUserIdAndIsRead(Integer userId, Boolean isRead);

    List<Notification> findByType(String type);

    List<Notification> findByUserIdAndType(Integer userId, String type);
}

