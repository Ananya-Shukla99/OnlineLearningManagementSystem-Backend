package com.edulearn.lesson.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileStorageService {

    @Value("${app.upload.path}")
    private String uploadPath;

    public String storeVideo(MultipartFile file, Integer courseId) throws IOException {
        Path uploadDir = Paths.get(uploadPath, "course-" + courseId);
        Files.createDirectories(uploadDir);

        String originalName = file.getOriginalFilename();
        String extension = originalName != null && originalName.contains(".")
                ? originalName.substring(originalName.lastIndexOf("."))
                : ".mp4";
        String uniqueFileName = UUID.randomUUID() + extension;

        Path targetPath = uploadDir.resolve(uniqueFileName);
        Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

        return "/api/v1/lessons/videos/course-" + courseId + "/" + uniqueFileName;
    }

    public String storePdf(MultipartFile file, Integer courseId) throws IOException {
        Path uploadDir = Paths.get(uploadPath, "pdfs", "course-" + courseId);
        Files.createDirectories(uploadDir);

        String originalName = file.getOriginalFilename();
        String extension = originalName != null && originalName.contains(".")
                ? originalName.substring(originalName.lastIndexOf("."))
                : ".pdf";
        String uniqueFileName = UUID.randomUUID() + extension;

        Path targetPath = uploadDir.resolve(uniqueFileName);
        Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

        return "/api/v1/lessons/files/pdfs/course-" + courseId + "/" + uniqueFileName;
    }
}
