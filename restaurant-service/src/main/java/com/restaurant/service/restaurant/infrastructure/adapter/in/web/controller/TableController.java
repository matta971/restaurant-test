package com.restaurant.service.restaurant.infrastructure.adapter.in.web.controller;

import com.restaurant.service.restaurant.domain.model.TableLocation;
import com.restaurant.service.restaurant.domain.port.in.TableManagementUseCase;
import com.restaurant.service.restaurant.infrastructure.adapter.in.web.dto.CreateTableRequest;
import com.restaurant.service.restaurant.infrastructure.adapter.in.web.dto.TableResponse;
import com.restaurant.service.restaurant.infrastructure.adapter.in.web.dto.UpdateTableRequest;
import com.restaurant.service.restaurant.infrastructure.adapter.in.web.mapper.TableWebMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

/**
 * REST Controller for Table management operations
 * Implements HATEOAS for resource navigation
 */
@RestController
@RequestMapping("/api/restaurants/{restaurantId}/tables")
@RequiredArgsConstructor
@Tag(name = "Table Management", description = "Operations for managing restaurant tables")
public class TableController {

    private final TableManagementUseCase tableManagementUseCase;
    private final TableWebMapper mapper;

    @Operation(
        summary = "Create a new table",
        description = "Creates a new table for the specified restaurant"
    )
    @ApiResponse(responseCode = "201", description = "Table created successfully")
    @ApiResponse(responseCode = "400", description = "Invalid input data")
    @ApiResponse(responseCode = "404", description = "Restaurant not found")
    @PostMapping
    public ResponseEntity<EntityModel<TableResponse>> createTable(
            @Parameter(description = "Restaurant ID") @PathVariable Long restaurantId,
            @Valid @RequestBody CreateTableRequest request) {
        
        var command = mapper.toCreateCommand(restaurantId, request);
        var table = tableManagementUseCase.createTable(command);
        var response = mapper.toResponse(table);
        
        var entityModel = EntityModel.of(response)
            .add(linkTo(methodOn(TableController.class).getTable(restaurantId, response.id())).withSelfRel())
            .add(linkTo(methodOn(TableController.class).getRestaurantTables(restaurantId)).withRel("tables"))
            .add(linkTo(methodOn(RestaurantController.class).getRestaurant(restaurantId)).withRel("restaurant"));
        
        return ResponseEntity.status(HttpStatus.CREATED).body(entityModel);
    }

    @Operation(
        summary = "Get table by ID",
        description = "Retrieves a table by its unique identifier"
    )
    @ApiResponse(responseCode = "200", description = "Table found")
    @ApiResponse(responseCode = "404", description = "Table not found")
    @GetMapping("/{tableId}")
    public ResponseEntity<EntityModel<TableResponse>> getTable(
            @Parameter(description = "Restaurant ID") @PathVariable Long restaurantId,
            @Parameter(description = "Table ID") @PathVariable Long tableId) {
        
        var table = tableManagementUseCase.getTable(tableId);
        var response = mapper.toResponse(table);
        
        var entityModel = EntityModel.of(response)
            .add(linkTo(methodOn(TableController.class).getTable(restaurantId, tableId)).withSelfRel())
            .add(linkTo(methodOn(TableController.class).getRestaurantTables(restaurantId)).withRel("tables"))
            .add(linkTo(methodOn(TableController.class).updateTable(restaurantId, tableId, null)).withRel("update"))
            .add(linkTo(methodOn(TableController.class).deleteTable(restaurantId, tableId)).withRel("delete"))
            .add(linkTo(methodOn(RestaurantController.class).getRestaurant(restaurantId)).withRel("restaurant"));
        
        if (table.isAvailable()) {
            entityModel.add(linkTo(methodOn(TableController.class).makeTableUnavailable(restaurantId, tableId)).withRel("makeUnavailable"));
        } else {
            entityModel.add(linkTo(methodOn(TableController.class).makeTableAvailable(restaurantId, tableId)).withRel("makeAvailable"));
        }
        
        return ResponseEntity.ok(entityModel);
    }

    @Operation(
        summary = "Get all tables for a restaurant",
        description = "Retrieves all tables for the specified restaurant"
    )
    @ApiResponse(responseCode = "200", description = "Tables retrieved successfully")
    @ApiResponse(responseCode = "404", description = "Restaurant not found")
    @GetMapping
    public ResponseEntity<CollectionModel<EntityModel<TableResponse>>> getRestaurantTables(
            @Parameter(description = "Restaurant ID") @PathVariable Long restaurantId) {
        
        var tables = tableManagementUseCase.getRestaurantTables(restaurantId);
        var responses = tables.stream().map(mapper::toResponse).toList();
        
        var entityModels = responses.stream()
            .map(response -> EntityModel.of(response)
                .add(linkTo(methodOn(TableController.class).getTable(restaurantId, response.id())).withSelfRel())
                .add(linkTo(methodOn(TableController.class).updateTable(restaurantId, response.id(), null)).withRel("update")))
            .toList();
        
        var collectionModel = CollectionModel.of(entityModels)
            .add(linkTo(methodOn(TableController.class).getRestaurantTables(restaurantId)).withSelfRel())
            .add(linkTo(methodOn(TableController.class).createTable(restaurantId, null)).withRel("create"))
            .add(linkTo(methodOn(TableController.class).getAvailableTables(restaurantId)).withRel("available"))
            .add(linkTo(methodOn(RestaurantController.class).getRestaurant(restaurantId)).withRel("restaurant"));
        
        return ResponseEntity.ok(collectionModel);
    }

    @Operation(
        summary = "Get available tables",
        description = "Retrieves all available tables for the specified restaurant"
    )
    @ApiResponse(responseCode = "200", description = "Available tables retrieved successfully")
    @GetMapping("/available")
    public ResponseEntity<CollectionModel<EntityModel<TableResponse>>> getAvailableTables(
            @Parameter(description = "Restaurant ID") @PathVariable Long restaurantId) {
        
        var tables = tableManagementUseCase.getAvailableTables(restaurantId);
        var responses = tables.stream().map(mapper::toResponse).toList();
        
        var entityModels = responses.stream()
            .map(response -> EntityModel.of(response)
                .add(linkTo(methodOn(TableController.class).getTable(restaurantId, response.id())).withSelfRel()))
            .toList();
        
        var collectionModel = CollectionModel.of(entityModels)
            .add(linkTo(methodOn(TableController.class).getAvailableTables(restaurantId)).withSelfRel())
            .add(linkTo(methodOn(TableController.class).getRestaurantTables(restaurantId)).withRel("all"));
        
        return ResponseEntity.ok(collectionModel);
    }

    @Operation(
        summary = "Get tables by location",
        description = "Retrieves tables by their location within the restaurant"
    )
    @ApiResponse(responseCode = "200", description = "Tables retrieved successfully")
    @GetMapping("/location/{location}")
    public ResponseEntity<CollectionModel<EntityModel<TableResponse>>> getTablesByLocation(
            @Parameter(description = "Restaurant ID") @PathVariable Long restaurantId,
            @Parameter(description = "Table location") @PathVariable TableLocation location) {
        
        var tables = tableManagementUseCase.getTablesByLocation(restaurantId, location);
        var responses = tables.stream().map(mapper::toResponse).toList();
        
        var entityModels = responses.stream()
            .map(response -> EntityModel.of(response)
                .add(linkTo(methodOn(TableController.class).getTable(restaurantId, response.id())).withSelfRel()))
            .toList();
        
        var collectionModel = CollectionModel.of(entityModels)
            .add(linkTo(methodOn(TableController.class).getTablesByLocation(restaurantId, location)).withSelfRel())
            .add(linkTo(methodOn(TableController.class).getRestaurantTables(restaurantId)).withRel("all"));
        
        return ResponseEntity.ok(collectionModel);
    }

    @Operation(
        summary = "Get tables by party size",
        description = "Retrieves tables that can accommodate the specified party size"
    )
    @ApiResponse(responseCode = "200", description = "Tables retrieved successfully")
    @GetMapping("/party-size/{partySize}")
    public ResponseEntity<CollectionModel<EntityModel<TableResponse>>> getTablesByPartySize(
            @Parameter(description = "Restaurant ID") @PathVariable Long restaurantId,
            @Parameter(description = "Party size") @PathVariable Integer partySize) {
        
        var tables = tableManagementUseCase.getTablesByPartySize(restaurantId, partySize);
        var responses = tables.stream().map(mapper::toResponse).toList();
        
        var entityModels = responses.stream()
            .map(response -> EntityModel.of(response)
                .add(linkTo(methodOn(TableController.class).getTable(restaurantId, response.id())).withSelfRel()))
            .toList();
        
        var collectionModel = CollectionModel.of(entityModels)
            .add(linkTo(methodOn(TableController.class).getTablesByPartySize(restaurantId, partySize)).withSelfRel())
            .add(linkTo(methodOn(TableController.class).getRestaurantTables(restaurantId)).withRel("all"));
        
        return ResponseEntity.ok(collectionModel);
    }

    @Operation(
        summary = "Update table",
        description = "Updates an existing table with new information"
    )
    @ApiResponse(responseCode = "200", description = "Table updated successfully")
    @ApiResponse(responseCode = "404", description = "Table not found")
    @ApiResponse(responseCode = "400", description = "Invalid input data")
    @PutMapping("/{tableId}")
    public ResponseEntity<EntityModel<TableResponse>> updateTable(
            @Parameter(description = "Restaurant ID") @PathVariable Long restaurantId,
            @Parameter(description = "Table ID") @PathVariable Long tableId,
            @Valid @RequestBody UpdateTableRequest request) {
        
        var command = mapper.toUpdateCommand(tableId, request);
        var table = tableManagementUseCase.updateTable(command);
        var response = mapper.toResponse(table);
        
        var entityModel = EntityModel.of(response)
            .add(linkTo(methodOn(TableController.class).getTable(restaurantId, tableId)).withSelfRel())
            .add(linkTo(methodOn(TableController.class).getRestaurantTables(restaurantId)).withRel("tables"))
            .add(linkTo(methodOn(TableController.class).deleteTable(restaurantId, tableId)).withRel("delete"));
        
        return ResponseEntity.ok(entityModel);
    }

    @Operation(
        summary = "Make table available",
        description = "Makes a table available for reservations"
    )
    @ApiResponse(responseCode = "200", description = "Table made available successfully")
    @ApiResponse(responseCode = "404", description = "Table not found")
    @PostMapping("/{tableId}/available")
    public ResponseEntity<EntityModel<TableResponse>> makeTableAvailable(
            @Parameter(description = "Restaurant ID") @PathVariable Long restaurantId,
            @Parameter(description = "Table ID") @PathVariable Long tableId) {
        
        var table = tableManagementUseCase.makeTableAvailable(tableId);
        var response = mapper.toResponse(table);
        
        var entityModel = EntityModel.of(response)
            .add(linkTo(methodOn(TableController.class).getTable(restaurantId, tableId)).withSelfRel())
            .add(linkTo(methodOn(TableController.class).makeTableUnavailable(restaurantId, tableId)).withRel("makeUnavailable"));
        
        return ResponseEntity.ok(entityModel);
    }

    @Operation(
        summary = "Make table unavailable",
        description = "Makes a table unavailable for reservations"
    )
    @ApiResponse(responseCode = "200", description = "Table made unavailable successfully")
    @ApiResponse(responseCode = "404", description = "Table not found")
    @PostMapping("/{tableId}/unavailable")
    public ResponseEntity<EntityModel<TableResponse>> makeTableUnavailable(
            @Parameter(description = "Restaurant ID") @PathVariable Long restaurantId,
            @Parameter(description = "Table ID") @PathVariable Long tableId) {
        
        var table = tableManagementUseCase.makeTableUnavailable(tableId);
        var response = mapper.toResponse(table);
        
        var entityModel = EntityModel.of(response)
            .add(linkTo(methodOn(TableController.class).getTable(restaurantId, tableId)).withSelfRel())
            .add(linkTo(methodOn(TableController.class).makeTableAvailable(restaurantId, tableId)).withRel("makeAvailable"));
        
        return ResponseEntity.ok(entityModel);
    }

    @Operation(
        summary = "Delete table",
        description = "Permanently deletes a table and all its associated data"
    )
    @ApiResponse(responseCode = "204", description = "Table deleted successfully")
    @ApiResponse(responseCode = "404", description = "Table not found")
    @DeleteMapping("/{tableId}")
    public ResponseEntity<Void> deleteTable(
            @Parameter(description = "Restaurant ID") @PathVariable Long restaurantId,
            @Parameter(description = "Table ID") @PathVariable Long tableId) {
        
        tableManagementUseCase.deleteTable(tableId);
        return ResponseEntity.noContent().build();
    }

    @Operation(
        summary = "Get table utilization statistics",
        description = "Retrieves utilization statistics for all tables in the restaurant"
    )
    @ApiResponse(responseCode = "200", description = "Statistics retrieved successfully")
    @ApiResponse(responseCode = "404", description = "Restaurant not found")
    @GetMapping("/stats")
    public ResponseEntity<EntityModel<TableManagementUseCase.TableUtilizationStats>> getTableUtilization(
            @Parameter(description = "Restaurant ID") @PathVariable Long restaurantId) {
        
        var stats = tableManagementUseCase.getTableUtilization(restaurantId);
        
        var entityModel = EntityModel.of(stats)
            .add(linkTo(methodOn(TableController.class).getTableUtilization(restaurantId)).withSelfRel())
            .add(linkTo(methodOn(TableController.class).getRestaurantTables(restaurantId)).withRel("tables"))
            .add(linkTo(methodOn(RestaurantController.class).getRestaurant(restaurantId)).withRel("restaurant"));
        
        return ResponseEntity.ok(entityModel);
    }

    @Operation(
        summary = "Get available table locations",
        description = "Retrieves all available table location types"
    )
    @ApiResponse(responseCode = "200", description = "Table locations retrieved successfully")
    @GetMapping("/locations")
    public ResponseEntity<CollectionModel<String>> getTableLocations(
            @Parameter(description = "Restaurant ID") @PathVariable Long restaurantId) {
        
        var locations = Arrays.stream(TableLocation.values())
            .map(Enum::name)
            .toList();
        
        var collectionModel = CollectionModel.of(locations)
            .add(linkTo(methodOn(TableController.class).getTableLocations(restaurantId)).withSelfRel());
        
        return ResponseEntity.ok(collectionModel);
    }
}