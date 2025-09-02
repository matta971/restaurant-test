package com.restaurant.service.restaurant.domain.config;

import com.restaurant.service.restaurant.domain.port.in.AvailabilityManagementUseCase;
import com.restaurant.service.restaurant.domain.port.in.RestaurantManagementUseCase;
import com.restaurant.service.restaurant.domain.port.in.TableManagementUseCase;
import com.restaurant.service.restaurant.domain.port.out.*;
import com.restaurant.service.restaurant.domain.service.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Domain layer configuration
 * Defines beans for use cases and domain services following hexagonal architecture
 */
@Configuration
public class DomainConfiguration {

    @Bean
    public AvailabilityService availabilityService() {
        return new AvailabilityService();
    }

    @Bean
    public RestaurantManagementUseCase restaurantManagementUseCase(
            RestaurantRepositoryPort restaurantRepository,
            EventPublisherPort eventPublisher) {
        return new RestaurantManagementUseCaseImpl(restaurantRepository, eventPublisher);
    }

    @Bean
    public TableManagementUseCase tableManagementUseCase(
            RestaurantTableRepositoryPort tableRepository,
            RestaurantRepositoryPort restaurantRepository,
            EventPublisherPort eventPublisher) {
        return new TableManagementUseCaseImpl(tableRepository, restaurantRepository, eventPublisher);
    }

    @Bean
    public AvailabilityManagementUseCase availabilityManagementUseCase(
            RestaurantRepositoryPort restaurantRepository,
            RestaurantTableRepositoryPort tableRepository,
            TimeSlotRepositoryPort timeSlotRepository,
            AvailabilityService availabilityService,
            EventPublisherPort eventPublisher,
            NotificationPort notificationPort) {
        return new AvailabilityManagementUseCaseImpl(
            restaurantRepository,
            tableRepository,
            timeSlotRepository,
            availabilityService,
            eventPublisher,
            notificationPort
        );
    }
}