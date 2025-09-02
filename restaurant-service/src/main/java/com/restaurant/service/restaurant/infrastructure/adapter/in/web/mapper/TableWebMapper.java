package com.restaurant.service.restaurant.infrastructure.adapter.in.web.mapper;

import com.restaurant.service.restaurant.domain.model.RestaurantTable;
import com.restaurant.service.restaurant.domain.port.in.TableManagementUseCase;
import com.restaurant.service.restaurant.infrastructure.adapter.in.web.dto.*;
import org.springframework.stereotype.Component;

/**
 * Mapper between Table domain objects and web DTOs
 */
@Component
public class TableWebMapper {

    public TableManagementUseCase.CreateTableCommand toCreateCommand(Long restaurantId, CreateTableRequest request) {
        return new TableManagementUseCase.CreateTableCommand(
            restaurantId,
            request.seats(),
            request.location()
        );
    }

    public TableManagementUseCase.UpdateTableCommand toUpdateCommand(Long id, UpdateTableRequest request) {
        return new TableManagementUseCase.UpdateTableCommand(
            id,
            request.seats(),
            request.location()
        );
    }

    public TableResponse toResponse(RestaurantTable table) {
        return new TableResponse(
            table.getId(),
            "T-"+table.getTableNumber(),
            table.getSeats(),
            table.getLocation(),
            table.isAvailable(),
            table.getRestaurant() != null ? table.getRestaurant().getId() : null
        );
    }
}