package com.restaurant.service.restaurant.infrastructure.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Configuration de sécurité temporaire pour le développement
 * Permet l'accès libre aux APIs pour les tests
 */
@Configuration
@EnableWebSecurity
@Profile({"docker", "dev"})
public class DevSecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(authz -> authz
                .requestMatchers("/api/**").permitAll()           // Permettre l'accès aux APIs
                .requestMatchers("/actuator/**").permitAll()      // Permettre l'accès aux actuators
                .requestMatchers("/h2-console/**").permitAll()    // Permettre l'accès à H2 console
                .requestMatchers("/swagger-ui/**").permitAll()    // Permettre l'accès à Swagger
                .requestMatchers("/api-docs/**").permitAll()      // Permettre l'accès aux docs API
                .anyRequest().authenticated()
            )
            .csrf(AbstractHttpConfigurer::disable)
            .headers(headers -> headers.frameOptions().disable());
        
        return http.build();
    }
}