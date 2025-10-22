package com.example.customer_service.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "customer.params")
public record CustomerConfigParams(int p1, int p2) {
}