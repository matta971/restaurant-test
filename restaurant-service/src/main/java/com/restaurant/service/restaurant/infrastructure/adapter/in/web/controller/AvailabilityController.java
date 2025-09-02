package com.restaurant.service.restaurant.infrastructure.adapter.in.web.controller;

import com.restaurant.service.restaurant.domain.model.TimeSlotStatus;
import com.restaurant.service.restaurant.domain.port.in.AvailabilityManagementUseCase;
import com.restaurant.service.restaurant.infrastructure.adapter.in.web.dto.*;
import com.restaurant.service.restaurant.infrastructure.adapter.in.web.mapper.AvailabilityWebMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

/**
 * REST Controller for Availability and Reservation management operations
 * Implements HATEOAS for resource navigation
 */
@RestController
@RequestMapping("/api/restaurants/{restaurantId}")
@RequiredArgsConstructor
@Tag(name = "Availability Management", description = "Operations for managing restaurant availability and reservations")
public class AvailabilityController {

    private final AvailabilityManagementUseCase availabilityManagementUseCase;
    private final AvailabilityWebMapper mapper;

    @Operation(
        summary = "Check availability",
        description = "Checks table availability for a specific date, time, and party size"
    )
    @ApiResponse(responseCode = "200", description = "Availability checked successfully")
    @ApiResponse(responseCode = "404", description = "Restaurant not found")
    @GetMapping("/availability")
    public ResponseEntity<CollectionModel<EntityModel<TableResponse>>> getAvailability(
            @Parameter(description = "Restaurant ID") @PathVariable Long restaurantId,
            @Parameter(description = "Reservation date") @RequestParam(required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @Parameter(description = "Start time") @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime startTime,
            @Parameter(description = "End time") @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime endTime,
            @Parameter(description = "Party size") @RequestParam(required = false) Integer partySize) {
        
        // Use defaults if not provided
        if (date == null) date = LocalDate.now().plusDays(1);
        if (startTime == null) startTime = LocalTime.of(19, 0);
        if (endTime == null) endTime = LocalTime.of(21, 0);
        if (partySize == null) partySize = 2;
        
        var query = new AvailabilityManagementUseCase.AvailabilityQuery(
            restaurantId, date, startTime, endTime, partySize);
        
        var availableTables = availabilityManagementUseCase.findAvailableTables(query);
        var responses = availableTables.stream().map(mapper::toTableResponse).toList();
        
        var entityModels = responses.stream()
            .map(response -> EntityModel.of(response)
                .add(linkTo(methodOn(TableController.class).getTable(restaurantId, response.id())).withSelfRel())
                .add(linkTo(methodOn(AvailabilityController.class).createReservation(restaurantId, null)).withRel("reserve")))
            .toList();
        
        var collectionModel = CollectionModel.of(entityModels)
            .add(linkTo(methodOn(AvailabilityController.class).getAvailability(
                restaurantId, date, startTime, endTime, partySize)).withSelfRel())
            .add(linkTo(methodOn(AvailabilityController.class).getBestTable(
                restaurantId, date, startTime, endTime, partySize)).withRel("bestTable"))
            .add(linkTo(methodOn(RestaurantController.class).getRestaurant(restaurantId)).withRel("restaurant"));
        
        return ResponseEntity.ok(collectionModel);
    }

    @Operation(
        summary = "Find best table",
        description = "Finds the best available table for the specified criteria"
    )
    @ApiResponse(responseCode = "200", description = "Best table found")
    @ApiResponse(responseCode = "404", description = "No tables available")
    @GetMapping("/best-table")
    public ResponseEntity<EntityModel<TableResponse>> getBestTable(
            @Parameter(description = "Restaurant ID") @PathVariable Long restaurantId,
            @Parameter(description = "Reservation date") @RequestParam 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @Parameter(description = "Start time") @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime startTime,
            @Parameter(description = "End time") @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime endTime,
            @Parameter(description = "Party size") @RequestParam Integer partySize) {
        
        var query = new AvailabilityManagementUseCase.AvailabilityQuery(
            restaurantId, date, startTime, endTime, partySize);
        
        var bestTable = availabilityManagementUseCase.findBestAvailableTable(query);
        var response = mapper.toTableResponse(bestTable);
        
        var entityModel = EntityModel.of(response)
            .add(linkTo(methodOn(AvailabilityController.class).getBestTable(
                restaurantId, date, startTime, endTime, partySize)).withSelfRel())
            .add(linkTo(methodOn(TableController.class).getTable(restaurantId, response.id())).withRel("table"))
            .add(linkTo(methodOn(AvailabilityController.class).createReservation(restaurantId, null)).withRel("reserve"));
        
        return ResponseEntity.ok(entityModel);
    }

    @Operation(
        summary = "Create reservation",
        description = "Creates a new reservation for the specified table and time"
    )
    @ApiResponse(responseCode = "201", description = "Reservation created successfully")
    @ApiResponse(responseCode = "400", description = "Invalid input data")
    @ApiResponse(responseCode = "404", description = "Restaurant or table not found")
    @ApiResponse(responseCode = "409", description = "Time slot not available")
    @PostMapping("/reservations")
    public ResponseEntity<EntityModel<TimeSlotResponse>> createReservation(
            @Parameter(description = "Restaurant ID") @PathVariable Long restaurantId,
            @Valid @RequestBody CreateReservationRequest request) {
        
        var command = mapper.toCreateReservationCommand(request);
        var timeSlot = availabilityManagementUseCase.createReservation(command);
        var response = mapper.toTimeSlotResponse(timeSlot);
        
        var entityModel = EntityModel.of(response)
            .add(linkTo(methodOn(AvailabilityController.class).getReservation(restaurantId, response.id())).withSelfRel())
            .add(linkTo(methodOn(AvailabilityController.class).getReservations(restaurantId, null)).withRel("reservations"))
            .add(linkTo(methodOn(AvailabilityController.class).confirmReservation(restaurantId, response.id())).withRel("confirm"))
            .add(linkTo(methodOn(TableController.class).getTable(restaurantId, response.tableId())).withRel("table"));
        
        return ResponseEntity.status(HttpStatus.CREATED).body(entityModel);
    }

    @Operation(
        summary = "Get reservation",
        description = "Retrieves a reservation by its ID"
    )
    @ApiResponse(responseCode = "200", description = "Reservation found")
    @ApiResponse(responseCode = "404", description = "Reservation not found")
    @GetMapping("/reservations/{reservationId}")
    public ResponseEntity<EntityModel<TimeSlotResponse>> getReservation(
            @Parameter(description = "Restaurant ID") @PathVariable Long restaurantId,
            @Parameter(description = "Reservation ID") @PathVariable Long reservationId) {
        
        // This would need to be implemented in the use case to find a specific time slot
        // For now, we'll throw an exception to indicate this needs implementation
        throw new UnsupportedOperationException("Get single reservation not yet implemented");
    }

    @Operation(
        summary = "Get reservations",
        description = "Retrieves reservations for a restaurant, optionally filtered by date"
    )
    @ApiResponse(responseCode = "200", description = "Reservations retrieved successfully")
    @ApiResponse(responseCode = "404", description = "Restaurant not found")
    @GetMapping("/reservations")
    public ResponseEntity<CollectionModel<EntityModel<TimeSlotResponse>>> getReservations(
            @Parameter(description = "Restaurant ID") @PathVariable Long restaurantId,
            @Parameter(description = "Filter by date") @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        
        var reservations = date != null 
            ? availabilityManagementUseCase.getReservationsForDate(restaurantId, date)
            : availabilityManagementUseCase.getUpcomingReservations(restaurantId);
        
        var responses = reservations.stream().map(mapper::toTimeSlotResponse).toList();
        
        var entityModels = responses.stream()
            .map(response -> EntityModel.of(response)
                .add(linkTo(methodOn(AvailabilityController.class).getReservation(restaurantId, response.id())).withSelfRel())
                .add(linkTo(methodOn(TableController.class).getTable(restaurantId, response.tableId())).withRel("table")))
            .toList();
        
        var collectionModel = CollectionModel.of(entityModels)
            .add(linkTo(methodOn(AvailabilityController.class).getReservations(restaurantId, date)).withSelfRel())
            .add(linkTo(methodOn(AvailabilityController.class).createReservation(restaurantId, null)).withRel("create"))
            .add(linkTo(methodOn(RestaurantController.class).getRestaurant(restaurantId)).withRel("restaurant"));
        
        return ResponseEntity.ok(collectionModel);
    }

    @Operation(
        summary = "Get reservations by status",
        description = "Retrieves reservations filtered by their status"
    )
    @ApiResponse(responseCode = "200", description = "Reservations retrieved successfully")
    @GetMapping("/reservations/status/{status}")
    public ResponseEntity<CollectionModel<EntityModel<TimeSlotResponse>>> getReservationsByStatus(
            @Parameter(description = "Restaurant ID") @PathVariable Long restaurantId,
            @Parameter(description = "Reservation status") @PathVariable TimeSlotStatus status) {
        
        var reservations = availabilityManagementUseCase.getReservationsByStatus(restaurantId, status);
        var responses = reservations.stream().map(mapper::toTimeSlotResponse).toList();
        
        var entityModels = responses.stream()
            .map(response -> EntityModel.of(response)
                .add(linkTo(methodOn(AvailabilityController.class).getReservation(restaurantId, response.id())).withSelfRel())
                .add(linkTo(methodOn(TableController.class).getTable(restaurantId, response.tableId())).withRel("table")))
            .toList();
        
        var collectionModel = CollectionModel.of(entityModels)
            .add(linkTo(methodOn(AvailabilityController.class).getReservationsByStatus(restaurantId, status)).withSelfRel())
            .add(linkTo(methodOn(AvailabilityController.class).getReservations(restaurantId, null)).withRel("all"));
        
        return ResponseEntity.ok(collectionModel);
    }

    @Operation(
        summary = "Confirm reservation",
        description = "Confirms a pending reservation"
    )
    @ApiResponse(responseCode = "200", description = "Reservation confirmed successfully")
    @ApiResponse(responseCode = "404", description = "Reservation not found")
    @ApiResponse(responseCode = "400", description = "Reservation cannot be confirmed")
    @PostMapping("/reservations/{reservationId}/confirm")
    public ResponseEntity<EntityModel<TimeSlotResponse>> confirmReservation(
            @Parameter(description = "Restaurant ID") @PathVariable Long restaurantId,
            @Parameter(description = "Reservation ID") @PathVariable Long reservationId) {
        
        var timeSlot = availabilityManagementUseCase.confirmReservation(reservationId);
        var response = mapper.toTimeSlotResponse(timeSlot);
        
        var entityModel = EntityModel.of(response)
            .add(linkTo(methodOn(AvailabilityController.class).getReservation(restaurantId, reservationId)).withSelfRel())
            .add(linkTo(methodOn(AvailabilityController.class).cancelReservation(restaurantId, reservationId)).withRel("cancel"))
            .add(linkTo(methodOn(AvailabilityController.class).completeReservation(restaurantId, reservationId)).withRel("complete"));
        
        return ResponseEntity.ok(entityModel);
    }

    @Operation(
        summary = "Cancel reservation",
        description = "Cancels an existing reservation"
    )
    @ApiResponse(responseCode = "200", description = "Reservation cancelled successfully")
    @ApiResponse(responseCode = "404", description = "Reservation not found")
    @ApiResponse(responseCode = "400", description = "Reservation cannot be cancelled")
    @PostMapping("/reservations/{reservationId}/cancel")
    public ResponseEntity<EntityModel<TimeSlotResponse>> cancelReservation(
            @Parameter(description = "Restaurant ID") @PathVariable Long restaurantId,
            @Parameter(description = "Reservation ID") @PathVariable Long reservationId) {
        
        var timeSlot = availabilityManagementUseCase.cancelReservation(reservationId);
        var response = mapper.toTimeSlotResponse(timeSlot);
        
        var entityModel = EntityModel.of(response)
            .add(linkTo(methodOn(AvailabilityController.class).getReservation(restaurantId, reservationId)).withSelfRel());
        
        return ResponseEntity.ok(entityModel);
    }

    @Operation(
        summary = "Complete reservation",
        description = "Marks a reservation as completed (service finished)"
    )
    @ApiResponse(responseCode = "200", description = "Reservation completed successfully")
    @ApiResponse(responseCode = "404", description = "Reservation not found")
    @ApiResponse(responseCode = "400", description = "Reservation cannot be completed")
    @PostMapping("/reservations/{reservationId}/complete")
    public ResponseEntity<EntityModel<TimeSlotResponse>> completeReservation(
            @Parameter(description = "Restaurant ID") @PathVariable Long restaurantId,
            @Parameter(description = "Reservation ID") @PathVariable Long reservationId) {
        
        var timeSlot = availabilityManagementUseCase.completeReservation(reservationId);
        var response = mapper.toTimeSlotResponse(timeSlot);
        
        var entityModel = EntityModel.of(response)
            .add(linkTo(methodOn(AvailabilityController.class).getReservation(restaurantId, reservationId)).withSelfRel());
        
        return ResponseEntity.ok(entityModel);
    }

    @Operation(
        summary = "Get capacity statistics",
        description = "Retrieves capacity and utilization statistics for the restaurant"
    )
    @ApiResponse(responseCode = "200", description = "Statistics retrieved successfully")
    @GetMapping("/capacity-stats")
    public ResponseEntity<EntityModel<AvailabilityManagementUseCase.CapacityStats>> getCapacityStats(
            @Parameter(description = "Restaurant ID") @PathVariable Long restaurantId) {
        
        var stats = availabilityManagementUseCase.getCapacityStats(restaurantId);
        
        var entityModel = EntityModel.of(stats)
            .add(linkTo(methodOn(AvailabilityController.class).getCapacityStats(restaurantId)).withSelfRel())
            .add(linkTo(methodOn(RestaurantController.class).getRestaurant(restaurantId)).withRel("restaurant"))
            .add(linkTo(methodOn(TableController.class).getRestaurantTables(restaurantId)).withRel("tables"));
        
        return ResponseEntity.ok(entityModel);
    }

    @Operation(
        summary = "Calculate availability rate",
        description = "Calculates the availability rate for a specific date"
    )
    @ApiResponse(responseCode = "200", description = "Availability rate calculated successfully")
    @GetMapping("/availability-rate")
    public ResponseEntity<EntityModel<AvailabilityRateResponse>> getAvailabilityRate(
            @Parameter(description = "Restaurant ID") @PathVariable Long restaurantId,
            @Parameter(description = "Date to check") @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        
        var rate = availabilityManagementUseCase.calculateAvailabilityRate(restaurantId, date);
        var response = new AvailabilityRateResponse(restaurantId, date, rate);
        
        var entityModel = EntityModel.of(response)
            .add(linkTo(methodOn(AvailabilityController.class).getAvailabilityRate(restaurantId, date)).withSelfRel())
            .add(linkTo(methodOn(RestaurantController.class).getRestaurant(restaurantId)).withRel("restaurant"));
        
        return ResponseEntity.ok(entityModel);
    }

    @Operation(
        summary = "Get available statuses",
        description = "Retrieves all available reservation status values"
    )
    @ApiResponse(responseCode = "200", description = "Statuses retrieved successfully")
    @GetMapping("/reservation-statuses")
    public ResponseEntity<CollectionModel<String>> getReservationStatuses(
            @Parameter(description = "Restaurant ID") @PathVariable Long restaurantId) {
        
        var statuses = Arrays.stream(TimeSlotStatus.values())
            .map(Enum::name)
            .toList();
        
        var collectionModel = CollectionModel.of(statuses)
            .add(linkTo(methodOn(AvailabilityController.class).getReservationStatuses(restaurantId)).withSelfRel());
        
        return ResponseEntity.ok(collectionModel);
    }
}