package com.edulearn.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NotificationDto implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long userId;
    private String type; // ENROLLMENT, PAYMENT, etc.
    private String title;
    private String message;
    private Long relatedEntityId;
    private String relatedEntityType;
}
