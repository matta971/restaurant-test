package com.restaurant.service.restaurant.infrastructure.adapter.in.web.dto;

import com.restaurant.service.restaurant.domain.model.TableLocation;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Table information response")
public record TableResponse(
    @Schema(description = "Table ID", example = "1")
    Long id,
    
    @Schema(description = "Table number", example = "T-12345678")
    String tableNumber,
    
    @Schema(description = "Number of seats", example = "4")
    Integer seats,
    
    @Schema(description = "Table location")
    TableLocation location,
    
    @Schema(description = "Is table available", example = "true")
    Boolean available,
    
    @Schema(description = "Restaurant ID", example = "1")
    Long restaurantId
) {}