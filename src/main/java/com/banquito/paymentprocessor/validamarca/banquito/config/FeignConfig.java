package com.banquito.paymentprocessor.validamarca.banquito.config;

import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableFeignClients(basePackages = "com.banquito.paymentprocessor.validamarca.banquito.client")
public class FeignConfig {
} 