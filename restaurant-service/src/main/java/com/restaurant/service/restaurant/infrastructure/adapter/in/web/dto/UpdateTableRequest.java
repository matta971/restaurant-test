package com.restaurant.service.restaurant.infrastructure.adapter.in.web.dto;

import com.restaurant.service.restaurant.domain.model.TableLocation;
import jakarta.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Request to update an existing table")
public record UpdateTableRequest(
    @Min(value = 1, message = "Table must have at least 1 seat")
    @Max(value = 8, message = "Table cannot have more than 8 seats")
    @Schema(description = "Number of seats", example = "4")
    Integer seats,
    
    @NotNull(message = "Table location is required")
    @Schema(description = "Table location")
    TableLocation location
) {}