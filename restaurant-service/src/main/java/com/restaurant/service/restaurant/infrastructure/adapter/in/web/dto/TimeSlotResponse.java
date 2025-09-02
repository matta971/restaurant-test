package com.restaurant.service.restaurant.infrastructure.adapter.in.web.dto;

import com.restaurant.service.restaurant.domain.model.TimeSlotStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.time.LocalTime;

@Schema(description = "Time slot reservation response")
public record TimeSlotResponse(
    @Schema(description = "Time slot ID", example = "1")
    Long id,
    
    @Schema(description = "Reservation date", example = "2024-01-15")
    LocalDate date,
    
    @Schema(description = "Start time", example = "19:00")
    LocalTime startTime,
    
    @Schema(description = "End time", example = "21:00")
    LocalTime endTime,
    
    @Schema(description = "Reserved seats", example = "4")
    Integer reservedSeats,
    
    @Schema(description = "Reservation status")
    TimeSlotStatus status,
    
    @Schema(description = "Table ID", example = "1")
    Long tableId,
    
    @Schema(description = "Duration in minutes", example = "120")
    Long durationInMinutes
) {}