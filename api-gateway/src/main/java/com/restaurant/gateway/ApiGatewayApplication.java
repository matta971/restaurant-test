package com.restaurant.gateway;

import org.springframework.beans.factory.annotation.Value;
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
    @Value("${services.restaurant.url:http://restaurant-service-1:8081}")
    private String restaurantServiceUrl;

    @Value("${services.reservation.url:http://reservation-service-1:8082}")
    private String reservationServiceUrl;

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
                // Restaurant Service Routes - URL configurée par variable
                .route("restaurant-service", r -> r
                        .path("/api/restaurants/**")
                        .filters(f -> f
                                .stripPrefix(0)
                                .addRequestHeader("X-Forwarded-Host", "localhost:8080")
                                .addRequestHeader("X-Forwarded-Proto", "http")
                                .addRequestHeader("X-Forwarded-Prefix", "")
                                .retry(config -> config
                                        .setRetries(3)
                                        .setStatuses(org.springframework.http.HttpStatus.BAD_GATEWAY,
                                                org.springframework.http.HttpStatus.GATEWAY_TIMEOUT)))
                        .uri(restaurantServiceUrl))

                // Reservation Service Routes - URL configurée par variable
                .route("reservation-service", r -> r
                        .path("/api/reservations/**", "/api/customers/**")
                        .filters(f -> f
                                .stripPrefix(0)
                                .addRequestHeader("X-Forwarded-Host", "localhost:8080")
                                .addRequestHeader("X-Forwarded-Proto", "http")
                                .addRequestHeader("X-Forwarded-Prefix", "")
                                .retry(config -> config
                                        .setRetries(3)
                                        .setStatuses(org.springframework.http.HttpStatus.BAD_GATEWAY,
                                                org.springframework.http.HttpStatus.GATEWAY_TIMEOUT)))
                        .uri(reservationServiceUrl))

                .build();
    }
}