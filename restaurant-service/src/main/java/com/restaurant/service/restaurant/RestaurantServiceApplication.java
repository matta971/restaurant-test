package com.restaurant.service.restaurant;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cloud.loadbalancer.annotation.LoadBalancerClient;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Restaurant Service Application - Manages restaurants and their availability
 * @author matt_
 */
@SpringBootApplication(scanBasePackages = {
    "com.restaurant.service.restaurant",
    "com.restaurant.common"
})
@EntityScan("com.restaurant.service.restaurant.domain.model")
@EnableJpaRepositories("com.restaurant.service.restaurant.infrastructure.adapter.out.persistence")
@LoadBalancerClient(name = "restaurant-service")
public class RestaurantServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(RestaurantServiceApplication.class, args);
    }
}