package com.edulearn.notification.dto;

import lombok.Data;
import java.io.Serializable;

@Data
public class NotificationDto implements Serializable {
    private Long userId;
    private String type;
    private String title;
    private String message;
    private Long relatedEntityId;
    private String relatedEntityType;
}
