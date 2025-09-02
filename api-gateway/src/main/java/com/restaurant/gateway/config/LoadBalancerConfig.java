package com.restaurant.gateway.config;

import org.springframework.cloud.client.DefaultServiceInstance;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.loadbalancer.core.ServiceInstanceListSupplier;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import reactor.core.publisher.Flux;

import java.util.Arrays;
import java.util.List;

/**
 * Load Balancer Configuration
 * Configures service instances for load balancing
 */
@Configuration
public class LoadBalancerConfig {

    @Bean
    @Primary
    ServiceInstanceListSupplier serviceInstanceListSupplier(ConfigurableApplicationContext context) {
        return new DemoServiceInstanceListSupplier("restaurant-service", "reservation-service");
    }

    /**
     * Demo service instance supplier for development
     * In production, use service discovery (Eureka, Consul, etc.)
     */
    static class DemoServiceInstanceListSupplier implements ServiceInstanceListSupplier {
        
        private final String[] serviceNames;

        DemoServiceInstanceListSupplier(String... serviceNames) {
            this.serviceNames = serviceNames;
        }

        @Override
        public String getServiceId() {
            return null;
        }

        @Override
        public Flux<List<ServiceInstance>> get() {
            return Flux.defer(() -> {
                List<ServiceInstance> restaurantInstances = Arrays.asList(
                        new DefaultServiceInstance("restaurant-service-1", "restaurant-service", 
                                "localhost", 8081, false),
                        new DefaultServiceInstance("restaurant-service-2", "restaurant-service", 
                                "localhost", 8082, false)
                );

                List<ServiceInstance> reservationInstances = Arrays.asList(
                        new DefaultServiceInstance("reservation-service-1", "reservation-service", 
                                "localhost", 8083, false),
                        new DefaultServiceInstance("reservation-service-2", "reservation-service", 
                                "localhost", 8084, false)
                );

                // For demo, return restaurant instances
                return Flux.just(restaurantInstances);
            });
        }
    }
}