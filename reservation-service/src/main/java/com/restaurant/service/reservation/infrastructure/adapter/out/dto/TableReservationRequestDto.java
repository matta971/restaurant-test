package com.restaurant.service.reservation.infrastructure.adapter.out.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * DTO for making reservation requests to Restaurant Service
 */
public record TableReservationRequestDto(
        
        @JsonProperty("restaurantId")
        Long restaurantId,
        
        @JsonProperty("tableId")
        Long tableId,
        
        @JsonProperty("date")
        LocalDate date,
        
        @JsonProperty("startTime")
        LocalTime startTime,
        
        @JsonProperty("endTime")
        LocalTime endTime,
        
        @JsonProperty("partySize")
        Integer partySize,
        
        @JsonProperty("customerEmail")
        String customerEmail,
        
        @JsonProperty("reservationId")
        Long reservationId
) {
}