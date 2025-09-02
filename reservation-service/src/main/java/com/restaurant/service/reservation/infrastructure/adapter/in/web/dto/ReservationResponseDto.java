package com.restaurant.service.reservation.infrastructure.adapter.in.web.dto;

import com.restaurant.service.reservation.domain.model.Reservation;
import com.restaurant.service.reservation.domain.model.ReservationStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * DTO for reservation responses
 */
@Schema(description = "Reservation response data")
public record ReservationResponseDto(
        
        @Schema(description = "Reservation ID", example = "1")
        Long id,
        
        @Schema(description = "Customer information")
        CustomerDto customer,
        
        @Schema(description = "Restaurant ID", example = "1")
        Long restaurantId,
        
        @Schema(description = "Table ID", example = "1")
        Long tableId,
        
        @Schema(description = "Reservation date", example = "2024-12-25")
        LocalDate reservationDate,
        
        @Schema(description = "Start time", example = "19:00")
        LocalTime startTime,
        
        @Schema(description = "End time", example = "21:00")
        LocalTime endTime,
        
        @Schema(description = "Number of people", example = "4")
        Integer partySize,
        
        @Schema(description = "Reservation status")
        ReservationStatus status,
        
        @Schema(description = "Special requests", example = "Vegetarian menu preferred")
        String specialRequests,
        
        @Schema(description = "When reservation was created")
        LocalDateTime createdAt,
        
        @Schema(description = "When reservation was confirmed")
        LocalDateTime confirmedAt,
        
        @Schema(description = "When reservation was cancelled")
        LocalDateTime cancelledAt,
        
        @Schema(description = "Cancellation reason")
        String cancellationReason,
        
        @Schema(description = "Duration in minutes", example = "120")
        Integer durationMinutes,
        
        @Schema(description = "Whether reservation can be modified")
        Boolean canBeModified,
        
        @Schema(description = "Whether reservation can be cancelled")
        Boolean canBeCancelled,
        
        @Schema(description = "Whether reservation is active")
        Boolean isActive
        
) {
    
    /**
     * Creates a ReservationResponseDto from a Reservation domain entity
     */
    public static ReservationResponseDto fromDomain(Reservation reservation) {
        return new ReservationResponseDto(
                reservation.getId(),
                CustomerDto.fromDomain(reservation.getCustomer()),
                reservation.getRestaurantId(),
                reservation.getTableId(),
                reservation.getReservationDate(),
                reservation.getStartTime(),
                reservation.getEndTime(),
                reservation.getPartySize(),
                reservation.getStatus(),
                reservation.getSpecialRequests(),
                reservation.getCreatedAt(),
                reservation.getConfirmedAt(),
                reservation.getCancelledAt(),
                reservation.getCancellationReason(),
                reservation.getDurationMinutes(),
                reservation.canBeModified(),
                reservation.canBeCancelled(),
                reservation.isActive()
        );
    }
}