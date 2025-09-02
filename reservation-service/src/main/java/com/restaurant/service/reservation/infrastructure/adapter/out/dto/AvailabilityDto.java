package com.restaurant.service.reservation.infrastructure.adapter.out.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * DTO for availability information from Restaurant Service
 */
public record AvailabilityDto(
        
        @JsonProperty("restaurantId")
        Long restaurantId,
        
        @JsonProperty("date")
        LocalDate date,
        
        @JsonProperty("available")
        Boolean available,
        
        @JsonProperty("availableTables")
        List<TableDto> availableTables,
        
        @JsonProperty("availableTimeSlots")
        List<TimeSlotDto> availableTimeSlots
) {
    
    /**
     * Time slot information
     */
    public record TimeSlotDto(
            @JsonProperty("startTime")
            LocalTime startTime,
            
            @JsonProperty("endTime")
            LocalTime endTime,
            
            @JsonProperty("availableSeats")
            Integer availableSeats
    ) {
    }
}