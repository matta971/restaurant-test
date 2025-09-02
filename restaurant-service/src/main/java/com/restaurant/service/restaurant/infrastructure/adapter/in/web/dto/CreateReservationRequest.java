package com.restaurant.service.restaurant.infrastructure.adapter.in.web.dto;

import jakarta.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.time.LocalTime;

@Schema(description = "Request to create a new reservation")
public record CreateReservationRequest(
    @NotNull(message = "Table ID is required")
    @Schema(description = "Table ID", example = "1")
    Long tableId,
    
    @NotNull(message = "Reservation date is required")
    @Schema(description = "Reservation date", example = "2024-01-15")
    LocalDate date,
    
    @NotNull(message = "Start time is required")
    @Schema(description = "Start time", example = "19:00")
    LocalTime startTime,
    
    @NotNull(message = "End time is required")
    @Schema(description = "End time", example = "21:00")
    LocalTime endTime,
    
    @Positive(message = "Party size must be positive")
    @Schema(description = "Party size", example = "4")
    Integer partySize
) {}