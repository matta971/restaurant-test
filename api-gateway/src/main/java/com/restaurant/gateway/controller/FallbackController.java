package com.restaurant.gateway.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Fallback controller for circuit breaker
 * Provides fallback responses when downstream services are unavailable
 */
@RestController
@RequestMapping("/fallback")
@Slf4j
public class FallbackController {

    @GetMapping("/restaurant")
    public Mono<ResponseEntity<Map<String, Object>>> restaurantFallback() {
        log.warn("Restaurant service fallback triggered");
        
        Map<String, Object> response = Map.of(
                "error", "Restaurant service is temporarily unavailable",
                "message", "Please try again later",
                "timestamp", LocalDateTime.now(),
                "service", "restaurant-service"
        );
        
        return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response));
    }

    @GetMapping("/reservation")
    public Mono<ResponseEntity<Map<String, Object>>> reservationFallback() {
        log.warn("Reservation service fallback triggered");
        
        Map<String, Object> response = Map.of(
                "error", "Reservation service is temporarily unavailable",
                "message", "Please try again later",
                "timestamp", LocalDateTime.now(),
                "service", "reservation-service"
        );
        
        return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response));
    }
}