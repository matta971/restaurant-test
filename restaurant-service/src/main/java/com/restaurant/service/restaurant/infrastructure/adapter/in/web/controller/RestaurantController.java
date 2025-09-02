package com.restaurant.service.restaurant.infrastructure.adapter.in.web.controller;

import com.restaurant.service.restaurant.domain.port.in.RestaurantManagementUseCase;
import com.restaurant.service.restaurant.infrastructure.adapter.in.web.dto.CreateRestaurantRequest;
import com.restaurant.service.restaurant.infrastructure.adapter.in.web.dto.RestaurantResponse;
import com.restaurant.service.restaurant.infrastructure.adapter.in.web.dto.UpdateRestaurantRequest;
import com.restaurant.service.restaurant.infrastructure.adapter.in.web.mapper.RestaurantWebMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

/**
 * REST Controller for Restaurant management operations
 * Implements HATEOAS for resource navigation
 */
@RestController
@RequestMapping("/api/restaurants")
@RequiredArgsConstructor
@Tag(name = "Restaurant Management", description = "Operations for managing restaurants")
public class RestaurantController {

    private final RestaurantManagementUseCase restaurantManagementUseCase;
    private final RestaurantWebMapper mapper;

    @Operation(
        summary = "Create a new restaurant",
        description = "Creates a new restaurant with the provided information"
    )
    @ApiResponse(responseCode = "201", description = "Restaurant created successfully")
    @ApiResponse(responseCode = "400", description = "Invalid input data")
    @PostMapping
    public ResponseEntity<EntityModel<RestaurantResponse>> createRestaurant(
            @Valid @RequestBody CreateRestaurantRequest request) {
        
        var command = mapper.toCreateCommand(request);
        var restaurant = restaurantManagementUseCase.createRestaurant(command);
        var response = mapper.toResponse(restaurant);
        
        var entityModel = EntityModel.of(response)
            .add(linkTo(methodOn(RestaurantController.class).getRestaurant(response.id())).withSelfRel())
            .add(linkTo(methodOn(RestaurantController.class).getAllRestaurants(Pageable.unpaged())).withRel("restaurants"))
            .add(linkTo(methodOn(TableController.class).getRestaurantTables(response.id())).withRel("tables"))
            .add(linkTo(methodOn(AvailabilityController.class).getAvailability(response.id(), null, null, null, null)).withRel("availability"));
        
        return ResponseEntity.status(HttpStatus.CREATED).body(entityModel);
    }

    @Operation(
        summary = "Get restaurant by ID",
        description = "Retrieves a restaurant by its unique identifier"
    )
    @ApiResponse(responseCode = "200", description = "Restaurant found")
    @ApiResponse(responseCode = "404", description = "Restaurant not found")
    @GetMapping("/{id}")
    public ResponseEntity<EntityModel<RestaurantResponse>> getRestaurant(
            @Parameter(description = "Restaurant ID") @PathVariable Long id) {
        
        var restaurant = restaurantManagementUseCase.getRestaurant(id);
        var response = mapper.toResponse(restaurant);
        
        var entityModel = EntityModel.of(response)
            .add(linkTo(methodOn(RestaurantController.class).getRestaurant(id)).withSelfRel())
            .add(linkTo(methodOn(RestaurantController.class).getAllRestaurants(Pageable.unpaged())).withRel("restaurants"))
            .add(linkTo(methodOn(RestaurantController.class).updateRestaurant(id, null)).withRel("update"))
            .add(linkTo(methodOn(RestaurantController.class).deleteRestaurant(id)).withRel("delete"))
            .add(linkTo(methodOn(TableController.class).getRestaurantTables(id)).withRel("tables"))
            .add(linkTo(methodOn(AvailabilityController.class).getAvailability(id, null, null, null, null)).withRel("availability"));
        
        if (restaurant.isActive()) {
            entityModel.add(linkTo(methodOn(RestaurantController.class).deactivateRestaurant(id)).withRel("deactivate"));
        } else {
            entityModel.add(linkTo(methodOn(RestaurantController.class).activateRestaurant(id)).withRel("activate"));
        }
        
        return ResponseEntity.ok(entityModel);
    }

    @Operation(
        summary = "Get all restaurants",
        description = "Retrieves all restaurants with pagination and sorting support"
    )
    @ApiResponse(responseCode = "200", description = "Restaurants retrieved successfully")
    @GetMapping
    public ResponseEntity<PagedModel<EntityModel<RestaurantResponse>>> getAllRestaurants(
            @PageableDefault(size = 20) Pageable pageable) {
        
        var restaurantPage = restaurantManagementUseCase.getAllRestaurants(pageable);
        var responsePage = restaurantPage.map(mapper::toResponse);
        
        var pagedModel = PagedModel.of(
            responsePage.getContent().stream()
                .map(response -> EntityModel.of(response)
                    .add(linkTo(methodOn(RestaurantController.class).getRestaurant(response.id())).withSelfRel())
                    .add(linkTo(methodOn(TableController.class).getRestaurantTables(response.id())).withRel("tables"))
                    .add(linkTo(methodOn(AvailabilityController.class).getAvailability(response.id(), null, null, null, null)).withRel("availability")))
                .toList(),
            new PagedModel.PageMetadata(
                responsePage.getSize(),
                responsePage.getNumber(),
                responsePage.getTotalElements(),
                responsePage.getTotalPages()
            )
        );
        
        pagedModel.add(linkTo(methodOn(RestaurantController.class).getAllRestaurants(pageable)).withSelfRel());
        pagedModel.add(linkTo(methodOn(RestaurantController.class).createRestaurant(null)).withRel("create"));
        pagedModel.add(linkTo(methodOn(RestaurantController.class).searchRestaurants(null, pageable)).withRel("search"));
        
        return ResponseEntity.ok(pagedModel);
    }

    @Operation(
        summary = "Search restaurants",
        description = "Search restaurants by name with pagination support"
    )
    @ApiResponse(responseCode = "200", description = "Search completed successfully")
    @GetMapping("/search")
    public ResponseEntity<PagedModel<EntityModel<RestaurantResponse>>> searchRestaurants(
            @Parameter(description = "Restaurant name to search for") @RequestParam(required = false) String name,
            @PageableDefault(size = 20) Pageable pageable) {
        
        var restaurantPage = restaurantManagementUseCase.searchRestaurants(name, pageable);
        var responsePage = restaurantPage.map(mapper::toResponse);
        
        var pagedModel = PagedModel.of(
            responsePage.getContent().stream()
                .map(response -> EntityModel.of(response)
                    .add(linkTo(methodOn(RestaurantController.class).getRestaurant(response.id())).withSelfRel())
                    .add(linkTo(methodOn(TableController.class).getRestaurantTables(response.id())).withRel("tables")))
                .toList(),
            new PagedModel.PageMetadata(
                responsePage.getSize(),
                responsePage.getNumber(),
                responsePage.getTotalElements(),
                responsePage.getTotalPages()
            )
        );
        
        pagedModel.add(linkTo(methodOn(RestaurantController.class).searchRestaurants(name, pageable)).withSelfRel());
        pagedModel.add(linkTo(methodOn(RestaurantController.class).getAllRestaurants(pageable)).withRel("all"));
        
        return ResponseEntity.ok(pagedModel);
    }

    @Operation(
        summary = "Update restaurant",
        description = "Updates an existing restaurant with new information"
    )
    @ApiResponse(responseCode = "200", description = "Restaurant updated successfully")
    @ApiResponse(responseCode = "404", description = "Restaurant not found")
    @ApiResponse(responseCode = "400", description = "Invalid input data")
    @PutMapping("/{id}")
    public ResponseEntity<EntityModel<RestaurantResponse>> updateRestaurant(
            @Parameter(description = "Restaurant ID") @PathVariable Long id,
            @Valid @RequestBody UpdateRestaurantRequest request) {
        
        var command = mapper.toUpdateCommand(id, request);
        var restaurant = restaurantManagementUseCase.updateRestaurant(command);
        var response = mapper.toResponse(restaurant);
        
        var entityModel = EntityModel.of(response)
            .add(linkTo(methodOn(RestaurantController.class).getRestaurant(id)).withSelfRel())
            .add(linkTo(methodOn(RestaurantController.class).getAllRestaurants(Pageable.unpaged())).withRel("restaurants"))
            .add(linkTo(methodOn(RestaurantController.class).deleteRestaurant(id)).withRel("delete"));
        
        return ResponseEntity.ok(entityModel);
    }

    @Operation(
        summary = "Activate restaurant",
        description = "Activates a restaurant making it available for reservations"
    )
    @ApiResponse(responseCode = "200", description = "Restaurant activated successfully")
    @ApiResponse(responseCode = "404", description = "Restaurant not found")
    @PostMapping("/{id}/activate")
    public ResponseEntity<EntityModel<RestaurantResponse>> activateRestaurant(
            @Parameter(description = "Restaurant ID") @PathVariable Long id) {
        
        var restaurant = restaurantManagementUseCase.activateRestaurant(id);
        var response = mapper.toResponse(restaurant);
        
        var entityModel = EntityModel.of(response)
            .add(linkTo(methodOn(RestaurantController.class).getRestaurant(id)).withSelfRel())
            .add(linkTo(methodOn(RestaurantController.class).deactivateRestaurant(id)).withRel("deactivate"));
        
        return ResponseEntity.ok(entityModel);
    }

    @Operation(
        summary = "Deactivate restaurant",
        description = "Deactivates a restaurant making it unavailable for new reservations"
    )
    @ApiResponse(responseCode = "200", description = "Restaurant deactivated successfully")
    @ApiResponse(responseCode = "404", description = "Restaurant not found")
    @PostMapping("/{id}/deactivate")
    public ResponseEntity<EntityModel<RestaurantResponse>> deactivateRestaurant(
            @Parameter(description = "Restaurant ID") @PathVariable Long id) {
        
        var restaurant = restaurantManagementUseCase.deactivateRestaurant(id);
        var response = mapper.toResponse(restaurant);
        
        var entityModel = EntityModel.of(response)
            .add(linkTo(methodOn(RestaurantController.class).getRestaurant(id)).withSelfRel())
            .add(linkTo(methodOn(RestaurantController.class).activateRestaurant(id)).withRel("activate"));
        
        return ResponseEntity.ok(entityModel);
    }

    @Operation(
        summary = "Delete restaurant",
        description = "Permanently deletes a restaurant and all its associated data"
    )
    @ApiResponse(responseCode = "204", description = "Restaurant deleted successfully")
    @ApiResponse(responseCode = "404", description = "Restaurant not found")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRestaurant(
            @Parameter(description = "Restaurant ID") @PathVariable Long id) {
        
        restaurantManagementUseCase.deleteRestaurant(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(
        summary = "Get restaurant statistics",
        description = "Retrieves operational statistics for a restaurant"
    )
    @ApiResponse(responseCode = "200", description = "Statistics retrieved successfully")
    @ApiResponse(responseCode = "404", description = "Restaurant not found")
    @GetMapping("/{id}/stats")
    public ResponseEntity<EntityModel<RestaurantManagementUseCase.RestaurantStats>> getRestaurantStats(
            @Parameter(description = "Restaurant ID") @PathVariable Long id) {
        
        var stats = restaurantManagementUseCase.getRestaurantStats(id);
        
        var entityModel = EntityModel.of(stats)
            .add(linkTo(methodOn(RestaurantController.class).getRestaurantStats(id)).withSelfRel())
            .add(linkTo(methodOn(RestaurantController.class).getRestaurant(id)).withRel("restaurant"));
        
        return ResponseEntity.ok(entityModel);
    }
}