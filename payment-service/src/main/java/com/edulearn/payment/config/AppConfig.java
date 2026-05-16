package com.edulearn.payment.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ComponentScan;

@Configuration
@ComponentScan(basePackages = {"com.edulearn.payment", "com.edulearn.notification"})
public class AppConfig {
}
