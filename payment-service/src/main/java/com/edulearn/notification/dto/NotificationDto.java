package com.edulearn.notification.dto;

import java.io.Serializable;

public class NotificationDto implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private Long userId;
    private String type;
    private String title;
    private String message;
    private Long relatedEntityId;
    private String relatedEntityType;

    public NotificationDto() {}

    public NotificationDto(Long userId, String type, String title, String message, Long relatedEntityId, String relatedEntityType) {
        this.userId = userId;
        this.type = type;
        this.title = title;
        this.message = message;
        this.relatedEntityId = relatedEntityId;
        this.relatedEntityType = relatedEntityType;
    }

    public static NotificationDtoBuilder builder() {
        return new NotificationDtoBuilder();
    }

    public static class NotificationDtoBuilder {
        private Long userId;
        private String type;
        private String title;
        private String message;
        private Long relatedEntityId;
        private String relatedEntityType;

        public NotificationDtoBuilder userId(Long userId) { this.userId = userId; return this; }
        public NotificationDtoBuilder type(String type) { this.type = type; return this; }
        public NotificationDtoBuilder title(String title) { this.title = title; return this; }
        public NotificationDtoBuilder message(String message) { this.message = message; return this; }
        public NotificationDtoBuilder relatedEntityId(Long relatedEntityId) { this.relatedEntityId = relatedEntityId; return this; }
        public NotificationDtoBuilder relatedEntityType(String relatedEntityType) { this.relatedEntityType = relatedEntityType; return this; }

        public NotificationDto build() {
            return new NotificationDto(userId, type, title, message, relatedEntityId, relatedEntityType);
        }
    }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public Long getRelatedEntityId() { return relatedEntityId; }
    public void setRelatedEntityId(Long relatedEntityId) { this.relatedEntityId = relatedEntityId; }
    public String getRelatedEntityType() { return relatedEntityType; }
    public void setRelatedEntityType(String relatedEntityType) { this.relatedEntityType = relatedEntityType; }
}
