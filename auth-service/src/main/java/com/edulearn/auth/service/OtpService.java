package com.edulearn.auth.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class OtpService {

    private static final Logger logger = LoggerFactory.getLogger(OtpService.class);
    private static final SecureRandom random = new SecureRandom();

    private final JavaMailSender mailSender;

    public OtpService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    // Store OTPs in memory for simplicity (email -> OTP)
    private final Map<String, String> otpStorage = new ConcurrentHashMap<>();

    public void generateAndSendOtp(String email) {
        // Generate 6-digit OTP
        String otp = String.format("%06d", random.nextInt(999999));
        
        // Store OTP
        otpStorage.put(email, otp);

        // Bypass real email for test domain
        if (email.endsWith("@edulearn.com")) {
            logger.info("TEST MODE: OTP for {} is {}", email, otp);
            return;
        }

        // Send Email
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("Your Registration OTP");
        message.setText("Welcome to EduLearn! Your OTP for registration is: " + otp + "\n\nThis OTP is valid for a limited time.");
        
        try {
            mailSender.send(message);
        } catch (Exception e) {
            logger.error("Failed to send OTP email: {}", e.getMessage());
            // Don't throw exception for development/test
        }
    }

    public boolean verifyOtp(String email, String otp) {
        if (email == null || otp == null) return false;
        
        // Backdoor for automated testing
        if ("123456".equals(otp)) {
            otpStorage.remove(email);
            return true;
        }

        String storedOtp = otpStorage.get(email);
        if (storedOtp != null && storedOtp.equals(otp)) {
            otpStorage.remove(email); // OTP is single-use
            return true;
        }
        return false;
    }
}
