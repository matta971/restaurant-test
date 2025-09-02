package com.restaurant.service.restaurant.infrastructure.adapter.in.web.mapper;

import com.restaurant.service.restaurant.domain.model.Restaurant;
import com.restaurant.service.restaurant.domain.port.in.RestaurantManagementUseCase;
import com.restaurant.service.restaurant.infrastructure.adapter.in.web.dto.*;
import org.springframework.stereotype.Component;

import java.time.LocalTime;

/**
 * Mapper between Restaurant domain objects and web DTOs
 */
@Component
public class RestaurantWebMapper {

    public RestaurantManagementUseCase.CreateRestaurantCommand toCreateCommand(CreateRestaurantRequest request) {
        return new RestaurantManagementUseCase.CreateRestaurantCommand(
            request.name(),
            request.address(),
            request.phoneNumber(),
            request.email(),
            request.capacity(),
            request.openingTime() != null ? request.openingTime() : LocalTime.of(11, 0),
            request.closingTime() != null ? request.closingTime() : LocalTime.of(23, 59)
        );
    }

    public RestaurantManagementUseCase.UpdateRestaurantCommand toUpdateCommand(Long id, UpdateRestaurantRequest request) {
        return new RestaurantManagementUseCase.UpdateRestaurantCommand(
            id,
            request.name(),
            request.address(),
            request.phoneNumber(),
            request.email(),
            request.capacity(),
            request.openingTime() != null ? request.openingTime() : LocalTime.of(11, 0),
            request.closingTime() != null ? request.closingTime() : LocalTime.of(23, 59)
        );
    }

    public RestaurantResponse toResponse(Restaurant restaurant) {
        return new RestaurantResponse(
            restaurant.getId(),
            restaurant.getName(),
            restaurant.getAddress(),
            restaurant.getPhoneNumber(),
            restaurant.getEmail(),
            restaurant.getCapacity(),
            restaurant.isActive(),
            restaurant.getOpeningTime(),
            restaurant.getClosingTime(),
            restaurant.getTotalAvailableSeats(),
            restaurant.getTables().size()
        );
    }
}