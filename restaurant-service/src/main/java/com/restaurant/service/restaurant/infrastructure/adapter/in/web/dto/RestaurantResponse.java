package com.restaurant.service.restaurant.infrastructure.adapter.in.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalTime;

@Schema(description = "Restaurant information response")
public record RestaurantResponse(
    @Schema(description = "Restaurant ID", example = "1")
    Long id,
    
    @Schema(description = "Restaurant name", example = "Le Petit Bistro")
    String name,
    
    @Schema(description = "Restaurant address", example = "123 Rue de la Paix, Paris")
    String address,
    
    @Schema(description = "Phone number", example = "+33 1 42 86 87 88")
    String phoneNumber,
    
    @Schema(description = "Email address", example = "contact@petitbistro.fr")
    String email,
    
    @Schema(description = "Restaurant capacity", example = "50")
    Integer capacity,
    
    @Schema(description = "Is restaurant active", example = "true")
    Boolean active,
    
    @Schema(description = "Opening time", example = "11:00")
    LocalTime openingTime,
    
    @Schema(description = "Closing time", example = "23:00")
    LocalTime closingTime,
    
    @Schema(description = "Total available seats", example = "48")
    Integer totalAvailableSeats,
    
    @Schema(description = "Number of tables", example = "12")
    Integer tableCount
) {}