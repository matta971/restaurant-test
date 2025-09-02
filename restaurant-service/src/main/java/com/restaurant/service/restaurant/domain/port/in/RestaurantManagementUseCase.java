package com.restaurant.service.restaurant.domain.port.in;

import com.restaurant.service.restaurant.domain.model.Restaurant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalTime;
import java.util.List;

/**
 * Use Case interface for Restaurant management operations
 * This defines the business operations available for restaurants
 */
public interface RestaurantManagementUseCase {

    /**
     * Creates a new restaurant
     * 
     * @param command the restaurant creation command
     * @return the created restaurant
     */
    Restaurant createRestaurant(CreateRestaurantCommand command);

    /**
     * Updates an existing restaurant
     * 
     * @param command the restaurant update command
     * @return the updated restaurant
     */
    Restaurant updateRestaurant(UpdateRestaurantCommand command);

    /**
     * Retrieves a restaurant by its ID
     * 
     * @param restaurantId the restaurant ID
     * @return the restaurant
     * @throws RestaurantNotFoundException if restaurant not found
     */
    Restaurant getRestaurant(Long restaurantId);

    /**
     * Retrieves all restaurants with pagination
     * 
     * @param pageable pagination information
     * @return page of restaurants
     */
    Page<Restaurant> getAllRestaurants(Pageable pageable);

    List<Restaurant> getAllRestaurants();
    List<Restaurant> getActiveRestaurants();
    /**
     * Searches restaurants by name with pagination
     * 
     * @param name the name to search for (can be partial)
     * @param pageable pagination information
     * @return page of matching restaurants
     */
    Page<Restaurant> searchRestaurants(String name, Pageable pageable);
    List<Restaurant> searchRestaurantsByName(String name);
    List<Restaurant> searchRestaurantsByCity(String city);
    /**
     * Activates a restaurant
     * 
     * @param restaurantId the restaurant ID
     * @return the activated restaurant
     */
    Restaurant activateRestaurant(Long restaurantId);

    /**
     * Deactivates a restaurant
     * 
     * @param restaurantId the restaurant ID
     * @return the deactivated restaurant
     */
    Restaurant deactivateRestaurant(Long restaurantId);

    /**
     * Deletes a restaurant
     * 
     * @param restaurantId the restaurant ID
     */
    void deleteRestaurant(Long restaurantId);

    /**
     * Gets restaurant statistics
     * 
     * @param restaurantId the restaurant ID
     * @return restaurant statistics
     */
    RestaurantStats getRestaurantStats(Long restaurantId);

    /**
     * Command for creating a restaurant
     */
    record CreateRestaurantCommand(
        String name,
        String address,
        String phoneNumber,
        String email,
        Integer capacity,
        LocalTime openingTime,
        LocalTime closingTime
    ) {}

    /**
     * Command for updating a restaurant
     */
    record UpdateRestaurantCommand(
        Long id,
        String name,
        String address,
        String phoneNumber,
        String email,
        Integer capacity,
        LocalTime openingTime,
        LocalTime closingTime
    ) {}

    /**
     * Restaurant statistics
     */
    record RestaurantStats(
        Long restaurantId,
        String name,
        Integer capacity,
        Integer totalTables,
        Integer availableTables,
        Integer totalSeats,
        Integer availableSeats,
        Boolean active,
        Double occupancyRate,
        Double averageTableSize
    ) {}

    /**
     * Exception thrown when restaurant is not found
     */
    class RestaurantNotFoundException extends RuntimeException {
        public RestaurantNotFoundException(Long restaurantId) {
            super("Restaurant not found with ID: " + restaurantId);
        }
    }

    class RestaurantAlreadyExistsException extends RuntimeException {
        public RestaurantAlreadyExistsException(String name) {
            super("Restaurant with name '" + name + "' already exists");
        }
    }
}