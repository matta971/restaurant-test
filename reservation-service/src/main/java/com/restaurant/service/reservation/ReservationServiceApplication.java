package com.restaurant.service.reservation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Main application class for Reservation Service
 * Enables Feign clients for restaurant service communication
 */
@SpringBootApplication
@EnableFeignClients
@EnableTransactionManagement
public class ReservationServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ReservationServiceApplication.class, args);
    }
}