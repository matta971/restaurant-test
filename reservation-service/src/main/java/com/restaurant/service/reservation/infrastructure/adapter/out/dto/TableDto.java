package com.restaurant.service.reservation.infrastructure.adapter.out.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO for Table information from Restaurant Service
 */
public record TableDto(
        
        @JsonProperty("id")
        Long id,
        
        @JsonProperty("restaurantId")
        Long restaurantId,
        
        @JsonProperty("seats")
        Integer seats,
        
        @JsonProperty("location")
        String location,
        
        @JsonProperty("available")
        Boolean available,
        
        @JsonProperty("tableNumber")
        String tableNumber
) {
}