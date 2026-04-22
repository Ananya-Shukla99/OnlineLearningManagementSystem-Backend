package com.edulearn.progress.service;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import com.edulearn.progress.entity.Progress;
import com.edulearn.progress.entity.Certificate;
import com.edulearn.progress.repository.ProgressRepository;
import com.edulearn.progress.repository.CertificateRepository;
import com.edulearn.notification.event.CertificateEvent;

/**
 * Implementation of ProgressService
 * Handles progress tracking and certificate generation with PDFBox
 */
@Service
public class ProgressServiceImpl implements ProgressService {

    @Autowired
    private ProgressRepository progressRepository;

    @Autowired
    private CertificateRepository certificateRepository;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Autowired
    private RestTemplate restTemplate;

    @Value("${certificate.output.path}")
    private String certificateOutputPath;

    @Value("${lesson.service.url}")
    private String lessonServiceUrl;

    @Override
    @Transactional
    public void trackProgress(Long studentId, Long courseId, Long lessonId, Integer watchedSeconds) {
        Optional<Progress> progressOpt = progressRepository.findByStudentIdAndLessonId(studentId, lessonId);

        Progress progress;
        if (progressOpt.isPresent()) {
            progress = progressOpt.get();
            if (watchedSeconds > progress.getWatchedSeconds()) {
                progress.setWatchedSeconds(watchedSeconds);
            }
        } else {
            progress = Progress.builder()
                    .studentId(studentId)
                    .courseId(courseId)
                    .lessonId(lessonId)
                    .watchedSeconds(watchedSeconds)
                    .isCompleted(false)
                    .build();
        }

        progress.setLastAccessedAt(LocalDateTime.now());
        progressRepository.save(progress);
    }

    @Override
    @Transactional
    public Map<String, Object> markLessonComplete(Long studentId, Long courseId, Long lessonId) {
        Optional<Progress> progressOpt = progressRepository.findByStudentIdAndLessonId(studentId, lessonId);

        Progress progress;
        if (progressOpt.isPresent()) {
            progress = progressOpt.get();
        } else {
            progress = Progress.builder()
                    .studentId(studentId)
                    .courseId(courseId)
                    .lessonId(lessonId)
                    .watchedSeconds(0)
                    .isCompleted(true)
                    .build();
        }

        progress.setIsCompleted(true);
        progress.setCompletedAt(LocalDateTime.now());
        progressRepository.save(progress);

        // Check if course is now fully complete and auto-issue certificate
        Integer courseProgress = getCourseProgress(studentId, courseId);
        Map<String, Object> result = new HashMap<>();
        result.put("lessonCompleted", true);
        result.put("courseProgress", courseProgress);

        if (courseProgress >= 100) {
            result.put("courseCompleted", true);
            try {
                Certificate certificate = issueCertificate(studentId, courseId);
                result.put("certificateIssued", true);
                result.put("certificate", certificate);
            } catch (Exception e) {
                // Certificate already issued or generation failed
                Optional<Certificate> existingCert = certificateRepository.findByStudentIdAndCourseId(studentId,
                        courseId);
                if (existingCert.isPresent()) {
                    result.put("certificateIssued", true);
                    result.put("certificate", existingCert.get());
                } else {
                    result.put("certificateIssued", false);
                    result.put("certificateError", e.getMessage());
                }
            }
        } else {
            result.put("courseCompleted", false);
        }

        // SYNC: Notify enrollment-service about the new progress percentage
        try {
            // enrollment-service is on 8084
            String enrollmentUrl = "http://localhost:8084/api/v1/enrollments/progress";
            Map<String, Object> syncReq = new HashMap<>();
            syncReq.put("studentId", studentId);
            syncReq.put("courseId", courseId);
            syncReq.put("progressPercent", courseProgress);
            restTemplate.put(enrollmentUrl, syncReq);
        } catch (Exception e) {
            System.err.println("Failed to sync progress to Enrollment Service: " + e.getMessage());
        }

        return result;
    }

    @Override
    public Integer getCourseProgress(Long studentId, Long courseId) {
        int completedLessons = progressRepository.countByStudentIdAndCourseIdAndIsCompleted(
                studentId, courseId, true);

        // Fetch actual total lessons from lesson-service via REST
        int totalLessons = 0;
        try {
            Integer count = restTemplate.getForObject(
                    "http://localhost:8083" + "/api/v1/lessons/count/" + courseId, Integer.class);
            totalLessons = (count != null && count > 0) ? count : 0;
        } catch (Exception e) {
            System.err.println("Could not reach lesson-service: " + e.getMessage());
        }

        Integer percentage = 0;
        if (totalLessons > 0) {
            percentage = (completedLessons * 100) / totalLessons;
        }

        // FALLBACK: If local progress is low, check enrollment-service
        if (percentage < 100) {
            try {
                Map<?, ?> enrollRes = restTemplate.getForObject(
                        "http://localhost:8084/api/v1/enrollments/student/" + studentId, Map.class);
                if (enrollRes != null && enrollRes.containsKey("data")) {
                    List<?> list = (List<?>) enrollRes.get("data");
                    for (Object obj : list) {
                        Map<?, ?> e = (Map<?, ?>) obj;
                        // Use a robust way to compare IDs (handle Integer vs Long)
                        Long enrollCourseId = e.get("courseId") != null ? Long.valueOf(e.get("courseId").toString())
                                : null;
                        if (Objects.equals(enrollCourseId, courseId)) {
                            Integer enrollPct = (Integer) e.get("progressPercent");
                            if (enrollPct != null && enrollPct > percentage) {
                                percentage = enrollPct;
                            }
                        }
                    }
                }
            } catch (Exception e) {
                System.err.println("Could not reach enrollment-service for fallback: " + e.getMessage());
            }
        }

        return Math.min(percentage, 100);
    }

    @Override
    public Optional<Progress> getLessonProgress(Long studentId, Long lessonId) {
        return progressRepository.findByStudentIdAndLessonId(studentId, lessonId);
    }

    @Override
    @Transactional
    public Certificate issueCertificate(Long studentId, Long courseId) {
        // 1. Check if certificate already exists
        if (certificateRepository.existsByStudentIdAndCourseId(studentId, courseId)) {
            System.out.println("DEBUG: Certificate already exists for Student: " + studentId + ", Course: " + courseId);
            return certificateRepository.findByStudentIdAndCourseId(studentId, courseId).orElse(null);
        }
        
        System.out.println("DEBUG: Issuing NEW certificate for Student: " + studentId + ", Course: " + courseId);



        // 2. Validate completion (Check granular progress first, then fallback to
        // enrollment service)
        Integer completionPercent = getCourseProgress(studentId, courseId);

        if (completionPercent < 100) {
            System.out.println("DEBUG: Granular progress is only " + completionPercent
                    + "%. Checking enrollment-service fallback...");
            try {
                // Fallback: Check enrollment-service directly (e.g. for manual completions)
                String enrollmentUrl = "http://localhost:8084/api/v1/enrollments/student/" + studentId;
                Map<String, Object> response = restTemplate.getForObject(enrollmentUrl, Map.class);
                List<?> enrollments = null;
                if (response != null && response.containsKey("data")) {
                    enrollments = (List<?>) response.get("data");
                }

                boolean foundComplete = false;
                if (enrollments != null) {
                    for (Object obj : enrollments) {
                        Map<?, ?> e = (Map<?, ?>) obj;
                        if (String.valueOf(e.get("courseId")).equals(String.valueOf(courseId))) {
                            Object pctObj = e.get("progressPercent");
                            if (pctObj != null && ((Number) pctObj).intValue() >= 100) {
                                foundComplete = true;
                                break;
                            }
                        }
                    }
                }
                if (!foundComplete) {
                    System.out.println("DEBUG: Enrollment check FAILED for user " + studentId + " - Course " + courseId + " is NOT 100%");
                    throw new RuntimeException("Course not yet fully completed (Enrollment says not 100%)");
                }
                System.out.println("DEBUG: Enrollment fallback confirmed 100% for user " + studentId + ". Proceeding with certificate generation.");

            } catch (Exception e) {
                if (e.getMessage().contains("Course not yet fully completed"))
                    throw (RuntimeException) e;
                System.err.println("Failed to verify completion via enrollment-service: " + e.getMessage());
                throw new RuntimeException("Course not yet fully completed and enrollment service unreachable");
            }
        }

        String verificationCode = generateVerificationCode(courseId);

        // Try to fetch course name from course-service
        String courseName = "Course " + courseId;
        String studentName = "Student #" + studentId;
        try {
            // Course name fetch
            Map<?, ?> courseRes = restTemplate.getForObject(
                    "http://localhost:8082" + "/api/v1/courses/" + courseId, Map.class);
            if (courseRes != null && courseRes.containsKey("data")) {
                Map<?, ?> data = (Map<?, ?>) courseRes.get("data");
                if (data != null && data.containsKey("title")) {
                    courseName = String.valueOf(data.get("title"));
                }
            }

            // Student name fetch from auth-service (Port 8081)
            Map<?, ?> userRes = restTemplate.getForObject(
                    "http://localhost:8081/auth/user/" + studentId, Map.class);
            
            if (userRes != null && userRes.get("data") != null) {
                Map<?, ?> data = (Map<?, ?>) userRes.get("data");
                if (data.get("fullName") != null) {
                    studentName = String.valueOf(data.get("fullName"));
                } else if (data.get("name") != null) {
                    studentName = String.valueOf(data.get("name"));
                }
            }
        } catch (Exception e) {
            System.err.println("Side-fetch for student name failed: " + e.getMessage());
            // Fallback: If direct service call fails, try Gateway (Port 8080)
            try {
                Map<?, ?> userRes = restTemplate.getForObject("http://localhost:8080/auth/user/" + studentId, Map.class);
                if (userRes != null && userRes.get("data") != null) {
                    Map<?, ?> data = (Map<?, ?>) userRes.get("data");
                    if (data.get("fullName") != null) studentName = String.valueOf(data.get("fullName"));
                }
            } catch (Exception e2) {
                System.err.println("Gateway fetch also failed: " + e2.getMessage());
            }
        }

        String pdfPath = generateCertificatePDF(studentId, courseId, verificationCode, courseName, studentName);
        String fileName = new File(pdfPath).getName();

        Certificate certificate = Certificate.builder()
                .studentId(studentId)
                .courseId(courseId)
                .issuedAt(LocalDate.now())
                .certificateUrl("/api/v1/progress/certificates/download/" + fileName)
                .verificationCode(verificationCode)
                .instructorName("Course Instructor")
                .courseName(courseName)
                .studentName(studentName)
                .build();

        Certificate savedCertificate = certificateRepository.save(certificate);

        // Disabled for stabilization: cross-service Spring events do not work across
        // JVM boundaries.
        // eventPublisher.publishEvent(
        // new CertificateEvent(this, studentId, "Course " + courseId, verificationCode)
        // );

        return savedCertificate;
    }

    private String generateVerificationCode(Long courseId) {
        LocalDate now = LocalDate.now();
        String year = String.valueOf(now.getYear());
        String categoryPrefix = "EL";
        String randomPart = UUID.randomUUID().toString().substring(0, 6).toUpperCase();

        return "EL-" + year + "-" + categoryPrefix + "-" + randomPart;
    }

    private String generateCertificatePDF(Long studentId, Long courseId, String verificationCode, String courseName,
            String studentName) {
        try {
            File certDir = new File(certificateOutputPath);
            if (!certDir.exists()) {
                certDir.mkdirs();
            }

            PDDocument document = new PDDocument();
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            PDPageContentStream content = new PDPageContentStream(document, page);

            float pageWidth = 595;
            float pageHeight = 842;

            // 1. BACKGROUND (WHITE)
            content.setNonStrokingColor(1.0f, 1.0f, 1.0f);
            content.addRect(0, 0, pageWidth, pageHeight);
            content.fill();

            // 2. CORNER DECORATIONS (RED POLYGONS)
            content.setNonStrokingColor(0.6f, 0.0f, 0.05f); // Deep Red

            // Top Left Triangle
            content.moveTo(0, pageHeight);
            content.lineTo(150, pageHeight);
            content.lineTo(0, pageHeight - 150);
            content.fill();

            // Top Right Triangle
            content.moveTo(pageWidth, pageHeight);
            content.lineTo(pageWidth - 150, pageHeight);
            content.lineTo(pageWidth, pageHeight - 150);
            content.fill();

            // Bottom Left Triangle
            content.moveTo(0, 0);
            content.lineTo(120, 0);
            content.lineTo(0, 120);
            content.fill();

            // Bottom Right Triangle
            content.moveTo(pageWidth, 0);
            content.lineTo(pageWidth - 120, 0);
            content.lineTo(pageWidth, 120);
            content.fill();

            // 3. SECONDARY RED STRIPES (Ribbon Style)
            content.setNonStrokingColor(0.8f, 0.0f, 0.1f); // Lighter Red
            content.moveTo(0, pageHeight - 30);
            content.lineTo(180, pageHeight);
            content.lineTo(210, pageHeight);
            content.lineTo(0, pageHeight - 60);
            content.fill();

            content.moveTo(pageWidth, 30);
            content.lineTo(pageWidth - 180, 0);
            content.lineTo(pageWidth - 210, 0);
            content.lineTo(pageWidth, 60);
            content.fill();

            // 4. TOP SEAL (BEST AWARD)
            content.setNonStrokingColor(0.7f, 0.5f, 0.1f); // Gold
            float sealX = pageWidth / 2;
            float sealY = 740;
            for (int i = 0; i < 30; i++) {
                double angle = 2 * Math.PI * i / 30;
                float x = (float) (sealX + 35 * Math.cos(angle));
                float y = (float) (sealY + 35 * Math.sin(angle));
                if (i == 0)
                    content.moveTo(x, y);
                else
                    content.lineTo(x, y);
            }
            content.fill();

            content.setNonStrokingColor(1.0f, 1.0f, 1.0f);
            content.setFont(PDType1Font.HELVETICA_BOLD, 10);
            String sealT1 = "BEST";
            float sealT1w = PDType1Font.HELVETICA_BOLD.getStringWidth(sealT1) / 1000 * 10;
            content.beginText();
            content.newLineAtOffset(sealX - (sealT1w / 2), sealY + 8);
            content.showText(sealT1);
            content.endText();

            String sealT2 = "AWARD";
            float sealT2w = PDType1Font.HELVETICA_BOLD.getStringWidth(sealT2) / 1000 * 9;
            content.setFont(PDType1Font.HELVETICA_BOLD, 9);
            content.beginText();
            content.newLineAtOffset(sealX - (sealT2w / 2), sealY - 8);
            content.showText(sealT2);
            content.endText();

            // 5. TITLE: "Certificate"
            content.setNonStrokingColor(0.0f, 0.0f, 0.0f);
            String titleStr = "Certificate";
            float titleSize = 64;
            content.setFont(PDType1Font.TIMES_BOLD_ITALIC, titleSize);
            float titleWidthOffset = PDType1Font.TIMES_BOLD_ITALIC.getStringWidth(titleStr) / 1000 * titleSize;
            content.beginText();
            content.newLineAtOffset((pageWidth - titleWidthOffset) / 2, 630);
            content.showText(titleStr);
            content.endText();

            // 6. SUBHEADER: "OF APPRECIATION"
            String subHeader = "OF APPRECIATION";
            content.setFont(PDType1Font.HELVETICA, 14);
            content.setNonStrokingColor(0.4f, 0.4f, 0.4f);
            float subW = PDType1Font.HELVETICA.getStringWidth(subHeader) / 1000 * 14;
            content.beginText();
            content.newLineAtOffset((pageWidth - subW) / 2, 605);
            content.showText(subHeader);
            content.endText();

            content.setStrokingColor(0.7f, 0.5f, 0.1f);
            content.setLineWidth(1);
            content.moveTo((pageWidth / 2) - 120, 595);
            content.lineTo((pageWidth / 2) + 120, 595);
            content.stroke();

            // 7. PRESENTED TO
            String prStr = "PROUDLY PRESENTED TO";
            content.setFont(PDType1Font.HELVETICA_BOLD, 11);
            content.setNonStrokingColor(0.5f, 0.5f, 0.5f);
            float prW = PDType1Font.HELVETICA_BOLD.getStringWidth(prStr) / 1000 * 11;
            content.beginText();
            content.newLineAtOffset((pageWidth - prW) / 2, 540);
            content.showText(prStr);
            content.endText();

            // 8. STUDENT NAME
            content.setNonStrokingColor(0.0f, 0.0f, 0.0f);
            float nsSize = 52;
            content.setFont(PDType1Font.TIMES_BOLD_ITALIC, nsSize);
            float nameWOffset = PDType1Font.TIMES_BOLD_ITALIC.getStringWidth(studentName) / 1000 * nsSize;
            content.beginText();
            content.newLineAtOffset((pageWidth - nameWOffset) / 2, 470);
            content.showText(studentName);
            content.endText();

            content.setStrokingColor(0.2f, 0.2f, 0.2f);
            content.setLineWidth(0.5f);
            content.moveTo((pageWidth / 2) - 180, 465);
            content.lineTo((pageWidth / 2) + 180, 465);
            content.stroke();

            // 9. DESCRIPTION
            content.setNonStrokingColor(0.4f, 0.4f, 0.4f);
            content.setFont(PDType1Font.TIMES_ROMAN, 13);
            String descText = "This certificate recognizes the successful completion of the course";
            float descW = PDType1Font.TIMES_ROMAN.getStringWidth(descText) / 1000 * 13;
            content.beginText();
            content.newLineAtOffset((pageWidth - descW) / 2, 420);
            content.showText(descText);
            content.endText();

            // 10. COURSE NAME
            content.setNonStrokingColor(0.0f, 0.0f, 0.0f);
            content.setFont(PDType1Font.TIMES_BOLD, 22);
            float courseWOffset = PDType1Font.TIMES_BOLD.getStringWidth(courseName) / 1000 * 22;
            content.beginText();
            content.newLineAtOffset((pageWidth - courseWOffset) / 2, 385);
            content.showText(courseName);
            content.endText();

            content.setNonStrokingColor(0.6f, 0.0f, 0.0f);
            content.setFont(PDType1Font.TIMES_BOLD, 15);
            content.beginText();
            content.newLineAtOffset((pageWidth / 2) - 45, 350);
            content.showText("EduLearn 2026");
            content.endText();

            // 11. SIGNATURE
            content.setStrokingColor(0.0f, 0.0f, 0.0f);
            content.setLineWidth(1f);
            content.moveTo((pageWidth / 2) - 80, 200);
            content.lineTo((pageWidth / 2) + 80, 200);
            content.stroke();

            content.setFont(PDType1Font.HELVETICA, 10);
            content.setNonStrokingColor(0.5f, 0.5f, 0.5f);
            String sigStr = "SIGNATURE";
            float sigWOffset = PDType1Font.HELVETICA.getStringWidth(sigStr) / 1000 * 10;
            content.beginText();
            content.newLineAtOffset((pageWidth - sigWOffset) / 2, 185);
            content.showText(sigStr);
            content.endText();

            // 12. FOOTER
            content.setNonStrokingColor(0.6f, 0.6f, 0.6f);
            content.setFont(PDType1Font.HELVETICA_OBLIQUE, 9);
            String issueDt = "Date: " + LocalDate.now();
            content.beginText();
            content.newLineAtOffset(60, 100);
            content.showText(issueDt);
            content.endText();

            String vLine = "Verify at: edulearn.com/verify | Code: " + verificationCode;
            float vWOffset = PDType1Font.HELVETICA_OBLIQUE.getStringWidth(vLine) / 1000 * 9;
            content.beginText();
            content.newLineAtOffset(pageWidth - vWOffset - 60, 100);
            content.showText(vLine);
            content.endText();

            content.close();

            String fileName = "cert_" + studentId + "_" + courseId + ".pdf";
            String filePath = certificateOutputPath + fileName;
            document.save(filePath);
            document.close();

            return filePath;
        } catch (IOException e) {
            throw new RuntimeException("Failed to generate certificate PDF: " + e.getMessage());
        }
    }

    @Override
    public Optional<Certificate> getCertificate(Long studentId, Long courseId) {
        return certificateRepository.findByStudentIdAndCourseId(studentId, courseId);
    }

    @Override
    public Optional<Certificate> verifyCertificate(String verificationCode) {
        return certificateRepository.findByVerificationCode(verificationCode);
    }

    @Override
    public List<Progress> getAllProgressByStudent(Long studentId) {
        return progressRepository.findByStudentId(studentId);
    }

    @Override
    public List<Certificate> getAllCertificatesByStudent(Long studentId) {
        return certificateRepository.findByStudentId(studentId);
    }
}
