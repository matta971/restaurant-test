package com.restaurant.service.reservation.infrastructure.adapter.out.client.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Request interceptor for Feign client calls
 * Adds common headers and logging
 */
@Component
@Slf4j
public class FeignRequestInterceptor implements RequestInterceptor {

    @Override
    public void apply(RequestTemplate template) {
        // Add common headers
        template.header("Content-Type", "application/json");
        template.header("Accept", "application/json");
        template.header("User-Agent", "Reservation-Service/1.0.0");
        
        // Add correlation ID for tracing (in real app, get from context)
        template.header("X-Correlation-ID", generateCorrelationId());
        
        log.debug("Feign request: {} {} with headers: {}", 
                template.method(), template.url(), template.headers());
    }

    private String generateCorrelationId() {
        // In real application, get from MDC or request context
        return "reservation-" + System.currentTimeMillis();
    }
}