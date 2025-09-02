package com.restaurant.service.restaurant.infrastructure.adapter.in.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;

@Schema(description = "Availability rate response")
public record AvailabilityRateResponse(
    @Schema(description = "Restaurant ID", example = "1")
    Long restaurantId,
    
    @Schema(description = "Date", example = "2024-01-15")
    LocalDate date,
    
    @Schema(description = "Availability rate (0.0 to 1.0)", example = "0.75")
    Double availabilityRate
) {}