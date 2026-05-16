package com.edulearn.payment.controller;

import com.edulearn.payment.entity.Payment;
import com.edulearn.payment.entity.Subscription;
import com.edulearn.payment.service.PaymentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.edulearn.payment.controller.PaymentController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

@WebMvcTest(PaymentController.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@TestPropertySource(properties = {
    "spring.cloud.discovery.enabled=false",
    "spring.cloud.config.enabled=false",
    "eureka.client.enabled=false"
})
@DisplayName("Payment Controller Tests")
class PaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PaymentService paymentService;
 
    @MockitoBean
    private com.edulearn.payment.config.JwtAuthenticationFilter jwtAuthenticationFilter;
 
    @MockitoBean
    private com.edulearn.payment.config.JwtTokenProvider jwtTokenProvider;
 
    @MockitoBean
    private org.springframework.amqp.rabbit.connection.ConnectionFactory connectionFactory;

    @MockitoBean
    private org.springframework.boot.web.client.RestTemplateBuilder restTemplateBuilder;

    @Autowired
    private ObjectMapper objectMapper;

    private Payment testPayment;
    private Subscription testSubscription;

    @BeforeEach
    void setUp() {
        objectMapper.registerModule(new JavaTimeModule());

        testPayment = Payment.builder()
                .paymentId(1L)
                .studentId(10L)
                .courseId(5L)
                .amount(1000.0)
                .status("SUCCESS")
                .build();

        testSubscription = Subscription.builder()
                .subscriptionId(1L)
                .studentId(10L)
                .plan("MONTHLY")
                .status("ACTIVE")
                .endDate(LocalDate.now().plusDays(30))
                .build();
    }

    @Test
    @DisplayName("POST /create-order - Success")
    void testCreateOrder() throws Exception {
        Map<String, String> orderResponse = Map.of("orderId", "order_123");
        when(paymentService.createOrder(anyLong(), anyLong(), anyDouble())).thenReturn(orderResponse);

        Map<String, Object> request = Map.of(
            "studentId", 10L,
            "courseId", 5L,
            "amount", 1000.0
        );

        mockMvc.perform(post("/api/v1/payments/create-order")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").value("order_123"));
    }

    @Test
    @DisplayName("POST /verify - Success")
    void testVerifyPayment() throws Exception {
        Map<String, Object> request = new HashMap<>();
        request.put("razorpayOrderId", "order_123");
        request.put("razorpayPaymentId", "pay_123");
        request.put("razorpaySignature", "sig_123");
        request.put("studentId", "10");
        request.put("courseId", "5");

        testPayment.setRazorpayOrderId("order_123");
        testPayment.setStudentId(10L);

        when(paymentService.verifyPayment(anyString(), anyString(), anyString(), anyLong(), anyLong()))
                .thenReturn(testPayment);

        mockMvc.perform(post("/api/v1/payments/verify")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"));
    }

    @Test
    @DisplayName("POST /subscriptions/subscribe - Success")
    void testSubscribe() throws Exception {
        when(paymentService.subscribe(10L, "MONTHLY")).thenReturn(testSubscription);

        Map<String, Object> request = Map.of(
            "studentId", 10L,
            "plan", "MONTHLY"
        );

        mockMvc.perform(post("/api/v1/payments/subscriptions/subscribe")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.plan").value("MONTHLY"));
    }

    @Test
    @DisplayName("DELETE /subscriptions/cancel/{studentId} - Success")
    void testCancelSubscription() throws Exception {
        doNothing().when(paymentService).cancelSubscription(10L);

        mockMvc.perform(delete("/api/v1/payments/subscriptions/cancel/10"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("GET /student/{studentId} - Success")
    void testGetHistory() throws Exception {
        when(paymentService.getPaymentHistory(10L)).thenReturn(List.of(testPayment));

        mockMvc.perform(get("/api/v1/payments/student/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].paymentId").value(1));
    }

    @Test
    @DisplayName("GET /subscriptions/student/{studentId} - Success")
    void testGetSubscription() throws Exception {
        when(paymentService.getSubscriptionByStudent(10L)).thenReturn(Optional.of(testSubscription));

        mockMvc.perform(get("/api/v1/payments/subscriptions/student/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.plan").value("MONTHLY"));
    }
}
