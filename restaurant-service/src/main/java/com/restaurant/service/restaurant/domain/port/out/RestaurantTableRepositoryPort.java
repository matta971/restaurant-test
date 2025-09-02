package com.restaurant.service.restaurant.domain.port.out;

import com.restaurant.service.restaurant.domain.model.RestaurantTable;
import com.restaurant.service.restaurant.domain.model.TableLocation;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

/**
 * Port (interface) for RestaurantTable persistence operations
 * This is part of the hexagonal architecture - defines the contract
 * that infrastructure adapters must implement
 */
public interface RestaurantTableRepositoryPort {

    /**
     * Saves a table (create or update)
     * 
     * @param table the table to save
     * @return the saved table with generated ID if new
     */
    RestaurantTable save(RestaurantTable table);

    /**
     * Finds a table by its ID
     * 
     * @param id the table ID
     * @return optional containing the table, empty if not found
     */
    Optional<RestaurantTable> findById(Long id);

    /**
     * Finds all tables for a specific restaurant
     * 
     * @param restaurantId the restaurant ID
     * @return list of tables for the restaurant
     */
    List<RestaurantTable> findByRestaurantId(Long restaurantId);

    /**
     * Finds available tables for a restaurant
     * 
     * @param restaurantId the restaurant ID
     * @return list of available tables
     */
    List<RestaurantTable> findAvailableByRestaurantId(Long restaurantId);

    /**
     * Finds tables by location type
     * 
     * @param restaurantId the restaurant ID
     * @param location the table location
     * @return list of tables at the specified location
     */
    List<RestaurantTable> findByRestaurantIdAndLocation(Long restaurantId, TableLocation location);

    /**
     * Finds tables by seat count
     * 
     * @param restaurantId the restaurant ID
     * @param seats the number of seats
     * @return list of tables with the specified seat count
     */
    List<RestaurantTable> findByRestaurantIdAndSeats(Long restaurantId, Integer seats);

    /**
     * Finds tables that can accommodate a party size
     * 
     * @param restaurantId the restaurant ID
     * @param partySize minimum number of seats needed
     * @return list of tables that can accommodate the party
     */
    List<RestaurantTable> findByRestaurantIdAndSeatsGreaterThanEqual(Long restaurantId, Integer partySize);

    /**
     * Finds available tables for a specific date and time range
     * 
     * @param restaurantId the restaurant ID
     * @param date the date
     * @param startTime the start time
     * @param endTime the end time
     * @param partySize minimum seats needed
     * @return list of available tables
     */
    List<RestaurantTable> findAvailableTablesForDateTimeAndPartySize(
        Long restaurantId, LocalDate date, LocalTime startTime, LocalTime endTime, Integer partySize);

    /**
     * Finds a table by its table number within a restaurant
     * 
     * @param restaurantId the restaurant ID
     * @param tableNumber the table number
     * @return optional containing the table, empty if not found
     */
    Optional<RestaurantTable> findByRestaurantIdAndTableNumber(Long restaurantId, String tableNumber);

    /**
     * Checks if a table exists with the given ID
     * 
     * @param id the table ID
     * @return true if exists, false otherwise
     */
    boolean existsById(Long id);

    /**
     * Deletes a table by ID
     * 
     * @param id the table ID to delete
     */
    void deleteById(Long id);

    /**
     * Counts tables for a restaurant
     * 
     * @param restaurantId the restaurant ID
     * @return table count
     */
    long countByRestaurantId(Long restaurantId);

    /**
     * Counts available tables for a restaurant
     * 
     * @param restaurantId the restaurant ID
     * @return available table count
     */
    long countAvailableByRestaurantId(Long restaurantId);
}