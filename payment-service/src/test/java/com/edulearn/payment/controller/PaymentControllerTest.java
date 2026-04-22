package com.edulearn.payment.controller;

import com.edulearn.payment.entity.Payment;
import com.edulearn.payment.entity.Subscription;
import com.edulearn.payment.service.PaymentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
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

import org.springframework.test.context.TestPropertySource;

@WebMvcTest(PaymentController.class)
@AutoConfigureMockMvc(addFilters = false)
@TestPropertySource(properties = {
    "spring.cloud.discovery.enabled=false",
    "spring.cloud.config.enabled=false",
    "eureka.client.enabled=false"
})
@DisplayName("Payment Controller Tests")
class PaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PaymentService paymentService;

    @MockBean
    private com.edulearn.payment.config.JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockBean
    private com.edulearn.payment.config.JwtTokenProvider jwtTokenProvider;

    @MockBean
    private org.springframework.amqp.rabbit.connection.ConnectionFactory connectionFactory;

    @Autowired
    private ObjectMapper objectMapper;

    private Payment testPayment;
    private Subscription testSubscription;

    @BeforeEach
    void setUp() {
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
    @DisplayName("POST /orders/create - Success")
    void testCreateOrder() throws Exception {
        Map<String, String> orderResponse = Map.of("orderId", "order_123");
        when(paymentService.createOrder(anyLong(), anyLong(), anyDouble())).thenReturn(orderResponse);

        mockMvc.perform(post("/api/v1/payment/orders/create/10/5/1000.0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").value("order_123"));
    }

    @Test
    @DisplayName("POST /verify - Success")
    void testVerifyPayment() throws Exception {
        Map<String, Object> request = new HashMap<>();
        request.put("razorpay_order_id", "order_123");
        request.put("razorpay_payment_id", "pay_123");
        request.put("razorpay_signature", "sig_123");
        request.put("studentId", 10L);
        request.put("courseId", 5L);

        when(paymentService.verifyPayment(anyString(), anyString(), anyString(), anyLong(), anyLong()))
                .thenReturn(testPayment);

        mockMvc.perform(post("/api/v1/payment/verify")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("POST /subscribe - Success")
    void testSubscribe() throws Exception {
        when(paymentService.subscribe(10L, "MONTHLY")).thenReturn(testSubscription);

        mockMvc.perform(post("/api/v1/payment/subscribe/10/MONTHLY"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("DELETE /subscribe/cancel/{studentId} - Success")
    void testCancelSubscription() throws Exception {
        doNothing().when(paymentService).cancelSubscription(10L);

        mockMvc.perform(delete("/api/v1/payment/subscribe/cancel/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("GET /history/{studentId} - Success")
    void testGetHistory() throws Exception {
        when(paymentService.getPaymentHistory(10L)).thenReturn(List.of(testPayment));

        mockMvc.perform(get("/api/v1/payment/history/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("GET /subscription/{studentId} - Success")
    void testGetSubscription() throws Exception {
        when(paymentService.getSubscriptionByStudent(10L)).thenReturn(Optional.of(testSubscription));

        mockMvc.perform(get("/api/v1/payment/subscription/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}
