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
            // Restaurant Service routes
            .route("restaurant-service", r -> r
                .path("/api/restaurants/**")
                .uri("lb://restaurant-service"))
            
            // Reservation Service routes
            .route("reservation-service", r -> r
                .path("/api/reservations/**")
                .uri("lb://reservation-service"))
            
            // Health check routes (direct access)
            .route("restaurant-health", r -> r
                .path("/restaurant-service/actuator/health")
                .uri("lb://restaurant-service"))
            
            .route("reservation-health", r -> r
                .path("/reservation-service/actuator/health")
                .uri("lb://reservation-service"))
            
            .build();
    }
}