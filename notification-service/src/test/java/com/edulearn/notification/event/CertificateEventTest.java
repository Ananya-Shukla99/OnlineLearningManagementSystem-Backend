package com.edulearn.notification.event;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CertificateEventTest {

    @Test
    void testCertificateEventProperties() {
        Object source = new Object();
        Long studentId = 123L;
        String courseName = "Java Programming";
        String verificationCode = "CERT-001";

        CertificateEvent event = new CertificateEvent(source, studentId, courseName, verificationCode);

        assertEquals(source, event.getSource());
        assertEquals(studentId, event.getStudentId());
        assertEquals(courseName, event.getCourseName());
        assertEquals(verificationCode, event.getVerificationCode());
    }
}
