package com.restaurant.service.restaurant.infrastructure.adapter.in.web.mapper;

import com.restaurant.service.restaurant.domain.model.RestaurantTable;
import com.restaurant.service.restaurant.domain.model.TimeSlot;
import com.restaurant.service.restaurant.domain.port.in.AvailabilityManagementUseCase;
import com.restaurant.service.restaurant.infrastructure.adapter.in.web.dto.*;
import org.springframework.stereotype.Component;

/**
 * Mapper between Availability domain objects and web DTOs
 */
@Component
public class AvailabilityWebMapper {

    public AvailabilityManagementUseCase.CreateReservationCommand toCreateReservationCommand(CreateReservationRequest request) {
        return new AvailabilityManagementUseCase.CreateReservationCommand(
            request.tableId(),
            request.date(),
            request.startTime(),
            request.endTime(),
            request.partySize(),
            request.customerEmail()
        );
    }

    public TableResponse toTableResponse(RestaurantTable table) {
        return new TableResponse(
            table.getId(),
            "T-"+table.getTableNumber(),
            table.getSeats(),
            table.getLocation(),
            table.isAvailable(),
            table.getRestaurant() != null ? table.getRestaurant().getId() : null
        );
    }

    public TimeSlotResponse toTimeSlotResponse(TimeSlot timeSlot) {
        return new TimeSlotResponse(
            timeSlot.getId(),
            timeSlot.getDate(),
            timeSlot.getStartTime(),
            timeSlot.getEndTime(),
            timeSlot.getReservedSeats(),
            timeSlot.getStatus(),
            timeSlot.getTable() != null ? timeSlot.getTable().getId() : null,
            timeSlot.getDurationInMinutes()
        );
    }
}