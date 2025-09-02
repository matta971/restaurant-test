package com.restaurant.service.reservation.domain.port.out;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * Port interface for communicating with Restaurant Service
 * This defines the contract for restaurant-related operations
 */
public interface RestaurantServiceClient {

    /**
     * Retrieves restaurant information by ID
     */
    RestaurantInfo getRestaurant(Long restaurantId);

    /**
     * Checks if a table exists and is available
     */
    boolean isTableAvailable(Long restaurantId, Long tableId, LocalDate date, 
                           LocalTime startTime, LocalTime endTime);

    /**
     * Retrieves available tables for given criteria
     */
    List<TableInfo> getAvailableTables(Long restaurantId, LocalDate date,
                                     LocalTime startTime, LocalTime endTime, Integer partySize);

    /**
     * Reserves a table (blocks availability)
     */
    boolean reserveTable(Long restaurantId, Long tableId, LocalDate date,
                        LocalTime startTime, LocalTime endTime);

    /**
     * Releases a table reservation (makes available again)
     */
    boolean releaseTableReservation(Long restaurantId, Long tableId, LocalDate date,
                                  LocalTime startTime, LocalTime endTime);

    /**
     * Restaurant information
     */
    record RestaurantInfo(
        Long id,
        String name,
        String address,
        String phoneNumber,
        String email,
        Integer capacity,
        boolean active
    ) {}

    /**
     * Table information
     */
    record TableInfo(
        Long id,
        Long restaurantId,
        Integer seats,
        String location,
        boolean available
    ) {}

    /**
     * Exception thrown when restaurant is not found
     */
    class RestaurantNotFoundException extends RuntimeException {
        public RestaurantNotFoundException(Long restaurantId) {
            super("Restaurant not found with ID: " + restaurantId);
        }
    }

    /**
     * Exception thrown when communication with restaurant service fails
     */
    class RestaurantServiceException extends RuntimeException {
        public RestaurantServiceException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}