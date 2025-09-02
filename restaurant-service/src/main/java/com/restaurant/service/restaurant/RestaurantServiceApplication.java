package com.restaurant.service.restaurant;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cloud.loadbalancer.annotation.LoadBalancerClient;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Restaurant Service Application - Manages restaurants and their availability
 * @author matt_
 */
@SpringBootApplication(scanBasePackages = {
    "com.restaurant.service.restaurant",
    "com.restaurant.common"
})
@EnableAsync
@LoadBalancerClient(name = "restaurant-service")
public class RestaurantServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(RestaurantServiceApplication.class, args);
    }
}