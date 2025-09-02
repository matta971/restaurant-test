package com.restaurant.service.restaurant.infrastructure.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class for infrastructure adapters
 * Ensures all adapters are properly scanned and registered as Spring beans
 */
@Configuration
@ComponentScan(basePackages = {
    "com.restaurant.service.restaurant.infrastructure.adapter.out.persistence",
    "com.restaurant.service.restaurant.infrastructure.adapter.out.event",
    "com.restaurant.service.restaurant.infrastructure.adapter.out.notification",
    "com.restaurant.service.restaurant.infrastructure.adapter.in.web"
})
public class AdapterConfig {
    
    // Configuration is handled by @ComponentScan
    // This class ensures that all our infrastructure adapters are properly registered
    
}