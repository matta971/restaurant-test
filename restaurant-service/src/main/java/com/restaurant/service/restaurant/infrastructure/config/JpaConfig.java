package com.restaurant.service.restaurant.infrastructure.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * JPA Configuration for Restaurant Service
 * Configures entity scanning and repository locations for the hexagonal architecture
 */
@Configuration
@EnableJpaRepositories(
    basePackages = "com.restaurant.service.restaurant.infrastructure.adapter.out.persistence"
)
@EntityScan(
    basePackages = "com.restaurant.service.restaurant.domain.model"
)
@EnableJpaAuditing
@EnableTransactionManagement
public class JpaConfig {
    
    // JPA configuration is handled by Spring Boot auto-configuration
    // This class exists to explicitly define the package scanning for our hexagonal architecture
}