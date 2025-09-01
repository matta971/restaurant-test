package com.restaurant.service.reservation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cloud.loadbalancer.annotation.LoadBalancerClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * @author matt_
 */
@SpringBootApplication(scanBasePackages = {
    "com.restaurant.service.reservation",
    "com.restaurant.common"
})
@EntityScan("com.restaurant.service.reservation.domain.model")
@EnableJpaRepositories("com.restaurant.service.reservation.infrastructure.adapter.out.persistence")
@EnableFeignClients("com.restaurant.service.reservation.infrastructure.adapter.out.client")
@LoadBalancerClient(name = "reservation-service")
public class ReservationServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ReservationServiceApplication.class, args);
    }
}