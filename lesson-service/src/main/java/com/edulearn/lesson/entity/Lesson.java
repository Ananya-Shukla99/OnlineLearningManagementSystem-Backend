package com.edulearn.lesson.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "lessons")
public class Lesson {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer lessonId;

    @Column(nullable = false)
    private Integer courseId;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String contentType;

    @Column(nullable = false)
    private String contentUrl;

    @Column(nullable = false)
    private Integer durationMinutes;

    @Column(nullable = false)
    private Integer orderIndex;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private Boolean isPreview = false;

    public Lesson() {
    }

    public Lesson(Integer lessonId, Integer courseId, String title, String contentType, String contentUrl,
                  Integer durationMinutes, Integer orderIndex, String description, Boolean isPreview) {
        this.lessonId = lessonId;
        this.courseId = courseId;
        this.title = title;
        this.contentType = contentType;
        this.contentUrl = contentUrl;
        this.durationMinutes = durationMinutes;
        this.orderIndex = orderIndex;
        this.description = description;
        this.isPreview = isPreview;
    }

    public Integer getLessonId() {
        return lessonId;
    }

    public void setLessonId(Integer lessonId) {
        this.lessonId = lessonId;
    }

    public Integer getCourseId() {
        return courseId;
    }

    public void setCourseId(Integer courseId) {
        this.courseId = courseId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public String getContentUrl() {
        return contentUrl;
    }

    public void setContentUrl(String contentUrl) {
        this.contentUrl = contentUrl;
    }

    public Integer getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(Integer durationMinutes) {
        this.durationMinutes = durationMinutes;
    }

    public Integer getOrderIndex() {
        return orderIndex;
    }

    public void setOrderIndex(Integer orderIndex) {
        this.orderIndex = orderIndex;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Boolean getIsPreview() {
        return isPreview;
    }

    public void setIsPreview(Boolean isPreview) {
        this.isPreview = isPreview;
    }
}
