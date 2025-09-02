package com.restaurant.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;

/**
 * API Gateway Application - Single entry point with JWT authentication
 * @author matt_
 */
@SpringBootApplication(scanBasePackages = {
    "com.restaurant.gateway",
    "com.restaurant.common"
})
public class ApiGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
    }

    /**
     * Configure routes for microservices
     * 
     * @param builder RouteLocatorBuilder for creating routes
     * @return RouteLocator with configured routes
     */
    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                // Restaurant Service Routes
                .route("restaurant-service", r -> r
                        .path("/api/restaurants/**")
                        .filters(f -> f
                                .stripPrefix(0)
                                .circuitBreaker(config -> config
                                        .setName("restaurant-service-cb")
                                        .setFallbackUri("forward:/fallback/restaurant"))
                                .retry(config -> config
                                        .setRetries(3)
                                        .setStatuses(org.springframework.http.HttpStatus.BAD_GATEWAY,
                                                org.springframework.http.HttpStatus.GATEWAY_TIMEOUT)))
                        .uri("lb://restaurant-service"))

                // Reservation Service Routes
                .route("reservation-service", r -> r
                        .path("/api/reservations/**")
                        .filters(f -> f
                                .stripPrefix(0)
                                .circuitBreaker(config -> config
                                        .setName("reservation-service-cb")
                                        .setFallbackUri("forward:/fallback/reservation"))
                                .retry(config -> config
                                        .setRetries(3)
                                        .setStatuses(org.springframework.http.HttpStatus.BAD_GATEWAY,
                                                org.springframework.http.HttpStatus.GATEWAY_TIMEOUT)))
                        .uri("lb://reservation-service"))

                // Authentication Routes (public)
                .route("auth", r -> r
                        .path("/auth/**")
                        .filters(f -> f.stripPrefix(0))
                        .uri("forward:///"))

                // Health Check Routes (public)
                .route("health", r -> r
                        .path("/actuator/health")
                        .filters(f -> f.stripPrefix(0))
                        .uri("forward:///"))

                // Swagger UI Routes (public for development)
                .route("swagger-ui", r -> r
                        .path("/swagger-ui/**", "/api-docs/**")
                        .filters(f -> f.stripPrefix(0))
                        .uri("forward:///"))

                .build();
    }
}