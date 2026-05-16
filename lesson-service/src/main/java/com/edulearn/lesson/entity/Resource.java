package com.edulearn.lesson.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "resources")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Resource {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer resourceId;

    @Column(nullable = false)
    private Integer lessonId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String fileUrl;

    @Column(nullable = false)
    private String fileType; // "PDF", "SLIDES", "CODE"

    @Column(nullable = false)
    private Long sizeKb;

    public Integer getResourceId() {
        return resourceId;
    }

    public void setResourceId(Integer resourceId) {
        this.resourceId = resourceId;
    }

    public Integer getLessonId() {
        return lessonId;
    }

    public void setLessonId(Integer lessonId) {
        this.lessonId = lessonId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFileUrl() {
        return fileUrl;
    }

    public void setFileUrl(String fileUrl) {
        this.fileUrl = fileUrl;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public Long getSizeKb() {
        return sizeKb;
    }

    public void setSizeKb(Long sizeKb) {
        this.sizeKb = sizeKb;
    }
}
