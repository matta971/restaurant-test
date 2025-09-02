package com.restaurant.service.reservation.infrastructure.adapter.out.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalTime;

/**
 * DTO for Restaurant information from Restaurant Service
 */
public record RestaurantDto(
        
        @JsonProperty("id")
        Long id,
        
        @JsonProperty("name")
        String name,
        
        @JsonProperty("address")
        String address,
        
        @JsonProperty("phoneNumber")
        String phoneNumber,
        
        @JsonProperty("email")
        String email,
        
        @JsonProperty("capacity")
        Integer capacity,
        
        @JsonProperty("openingTime")
        LocalTime openingTime,
        
        @JsonProperty("closingTime")
        LocalTime closingTime,
        
        @JsonProperty("active")
        Boolean active
) {
}