package com.restaurant.service.restaurant.infrastructure.adapter.in.web.dto;

import jakarta.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalTime;

@Schema(description = "Request to update an existing restaurant")
public record UpdateRestaurantRequest(
    @NotBlank(message = "Restaurant name is required")
    @Schema(description = "Restaurant name", example = "Le Petit Bistro")
    String name,
    
    @NotBlank(message = "Restaurant address is required")
    @Schema(description = "Restaurant address", example = "123 Rue de la Paix, Paris")
    String address,
    
    @Schema(description = "Phone number", example = "+33 1 42 86 87 88")
    String phoneNumber,
    
    @Email(message = "Valid email format required")
    @Schema(description = "Email address", example = "contact@petitbistro.fr")
    String email,
    
    @Positive(message = "Capacity must be positive")
    @Schema(description = "Restaurant capacity", example = "50")
    Integer capacity,
    
    @Schema(description = "Opening time", example = "11:00")
    LocalTime openingTime,
    
    @Schema(description = "Closing time", example = "23:00")
    LocalTime closingTime
) {}