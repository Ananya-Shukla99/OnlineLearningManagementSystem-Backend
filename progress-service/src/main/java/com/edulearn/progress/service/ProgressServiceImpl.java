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

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

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

    @Value("${certificate.output.path}")
    private String certificateOutputPath;

    @Override
    @Transactional
    public void trackProgress(Integer studentId, Integer courseId, Integer lessonId, Integer watchedSeconds) {
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
    public void markLessonComplete(Integer studentId, Integer courseId, Integer lessonId) {
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
    }

    @Override
    public Integer getCourseProgress(Integer studentId, Integer courseId) {
        int completedLessons = progressRepository.countByStudentIdAndCourseIdAndIsCompleted(
                studentId, courseId, true);

        int totalLessons = 3;

        if (totalLessons == 0) {
            return 0;
        }

        Integer percentage = (completedLessons * 100) / totalLessons;
        return Math.min(percentage, 100);
    }

    @Override
    public Optional<Progress> getLessonProgress(Integer studentId, Integer lessonId) {
        return progressRepository.findByStudentIdAndLessonId(studentId, lessonId);
    }

    @Override
    @Transactional
    public Certificate issueCertificate(Integer studentId, Integer courseId) {
        if (certificateRepository.existsByStudentIdAndCourseId(studentId, courseId)) {
            return certificateRepository.findByStudentIdAndCourseId(studentId, courseId)
                    .orElseThrow(() -> new RuntimeException("Certificate already issued"));
        }

        String verificationCode = generateVerificationCode(courseId);
        String pdfPath = generateCertificatePDF(studentId, courseId, verificationCode);

        Certificate certificate = Certificate.builder()
                .studentId(studentId)
                .courseId(courseId)
                .issuedAt(LocalDate.now())
                .certificateUrl(pdfPath)
                .verificationCode(verificationCode)
                .instructorName("Course Instructor")
                .courseName("Course " + courseId)
                .build();

        Certificate savedCertificate = certificateRepository.save(certificate);

        // Publish certificate event - NotificationServiceImpl will listen and create notification
        eventPublisher.publishEvent(
                new CertificateEvent(this, studentId, "Course " + courseId, verificationCode)
        );

        return savedCertificate;
    }

    private String generateVerificationCode(Integer courseId) {
        LocalDate now = LocalDate.now();
        String year = String.valueOf(now.getYear());
        String categoryPrefix = "EL";
        String randomPart = UUID.randomUUID().toString().substring(0, 6).toUpperCase();

        return "EL-" + year + "-" + categoryPrefix + "-" + randomPart;
    }

    private String generateCertificatePDF(Integer studentId, Integer courseId, String verificationCode) {
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

            content.setStrokingColor(0.1f, 0.4f, 0.7f);
            content.setLineWidth(3);
            content.addRect(20, 20, pageWidth - 40, pageHeight - 40);
            content.stroke();

            String title = "CERTIFICATE OF COMPLETION";
            float titleFont = 28;
            float titleWidth = PDType1Font.HELVETICA_BOLD.getStringWidth(title) / 1000 * titleFont;
            float titleX = (pageWidth - titleWidth) / 2;
            content.setFont(PDType1Font.HELVETICA_BOLD, titleFont);
            content.setNonStrokingColor(0.1f, 0.4f, 0.7f);
            content.beginText();
            content.newLineAtOffset(titleX, 720);
            content.showText(title);
            content.endText();

            String line1 = "This certifies that";
            float line1Width = PDType1Font.HELVETICA.getStringWidth(line1) / 1000 * 16;
            float line1X = (pageWidth - line1Width) / 2;
            content.setFont(PDType1Font.HELVETICA, 16);
            content.setNonStrokingColor(0, 0, 0);
            content.beginText();
            content.newLineAtOffset(line1X, 650);
            content.showText(line1);
            content.endText();

            String studentName = "Student ID: " + studentId;
            float nameWidth = PDType1Font.HELVETICA_BOLD.getStringWidth(studentName) / 1000 * 24;
            float nameX = (pageWidth - nameWidth) / 2;
            content.setFont(PDType1Font.HELVETICA_BOLD, 24);
            content.beginText();
            content.newLineAtOffset(nameX, 610);
            content.showText(studentName);
            content.endText();

            String line2 = "has successfully completed";
            float line2Width = PDType1Font.HELVETICA.getStringWidth(line2) / 1000 * 16;
            float line2X = (pageWidth - line2Width) / 2;
            content.setFont(PDType1Font.HELVETICA, 16);
            content.beginText();
            content.newLineAtOffset(line2X, 570);
            content.showText(line2);
            content.endText();

            String courseName = "Course " + courseId;
            float courseWidth = PDType1Font.HELVETICA_BOLD.getStringWidth(courseName) / 1000 * 20;
            float courseX = (pageWidth - courseWidth) / 2;
            content.setFont(PDType1Font.HELVETICA_BOLD, 20);
            content.beginText();
            content.newLineAtOffset(courseX, 530);
            content.showText(courseName);
            content.endText();

            String instructorLine = "Instructed by: EduLearn";
            float instrWidth = PDType1Font.HELVETICA.getStringWidth(instructorLine) / 1000 * 14;
            float instrX = (pageWidth - instrWidth) / 2;
            content.setFont(PDType1Font.HELVETICA, 14);
            content.beginText();
            content.newLineAtOffset(instrX, 480);
            content.showText(instructorLine);
            content.endText();

            String issueLine = "Issued on: " + LocalDate.now();
            float issueWidth = PDType1Font.HELVETICA.getStringWidth(issueLine) / 1000 * 14;
            float issueX = (pageWidth - issueWidth) / 2;
            content.beginText();
            content.newLineAtOffset(issueX, 450);
            content.showText(issueLine);
            content.endText();

            String verifyLine = "Verification Code: " + verificationCode;
            float verifyWidth = PDType1Font.HELVETICA.getStringWidth(verifyLine) / 1000 * 11;
            float verifyX = (pageWidth - verifyWidth) / 2;
            content.setFont(PDType1Font.HELVETICA, 11);
            content.setNonStrokingColor(0.5f, 0.5f, 0.5f);
            content.beginText();
            content.newLineAtOffset(verifyX, 100);
            content.showText(verifyLine);
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
    public Optional<Certificate> getCertificate(Integer studentId, Integer courseId) {
        return certificateRepository.findByStudentIdAndCourseId(studentId, courseId);
    }

    @Override
    public Optional<Certificate> verifyCertificate(String verificationCode) {
        return certificateRepository.findByVerificationCode(verificationCode);
    }

    @Override
    public List<Progress> getAllProgressByStudent(Integer studentId) {
        return progressRepository.findByStudentId(studentId);
    }

    @Override
    public List<Certificate> getAllCertificatesByStudent(Integer studentId) {
        return certificateRepository.findByStudentId(studentId);
    }
}

