package com.restaurant.service.restaurant.domain.port.in;

import com.restaurant.service.restaurant.domain.model.RestaurantTable;
import com.restaurant.service.restaurant.domain.model.TableLocation;
import java.util.List;

/**
 * Use Case interface for Table management operations
 * This defines the business operations available for restaurant tables
 */
public interface TableManagementUseCase {

    /**
     * Creates a new table for a restaurant
     * 
     * @param command the table creation command
     * @return the created table
     */
    RestaurantTable createTable(CreateTableCommand command);

    /**
     * Updates an existing table
     * 
     * @param command the table update command
     * @return the updated table
     */
    RestaurantTable updateTable(UpdateTableCommand command);

    /**
     * Retrieves a table by its ID
     * 
     * @param tableId the table ID
     * @return the table
     * @throws TableNotFoundException if table not found
     */
    RestaurantTable getTable(Long tableId);

    /**
     * Retrieves all tables for a restaurant
     * 
     * @param restaurantId the restaurant ID
     * @return list of tables for the restaurant
     */
    List<RestaurantTable> getRestaurantTables(Long restaurantId);

    /**
     * Retrieves available tables for a restaurant
     * 
     * @param restaurantId the restaurant ID
     * @return list of available tables
     */
    List<RestaurantTable> getAvailableTables(Long restaurantId);

    /**
     * Retrieves tables by location type
     * 
     * @param restaurantId the restaurant ID
     * @param location the table location
     * @return list of tables at the specified location
     */
    List<RestaurantTable> getTablesByLocation(Long restaurantId, TableLocation location);

    /**
     * Retrieves tables that can accommodate a party size
     * 
     * @param restaurantId the restaurant ID
     * @param partySize minimum number of seats needed
     * @return list of suitable tables
     */
    List<RestaurantTable> getTablesByPartySize(Long restaurantId, Integer partySize);

    /**
     * Makes a table available
     * 
     * @param tableId the table ID
     * @return the updated table
     */
    RestaurantTable makeTableAvailable(Long tableId);

    /**
     * Makes a table unavailable
     * 
     * @param tableId the table ID
     * @return the updated table
     */
    RestaurantTable makeTableUnavailable(Long tableId);

    /**
     * Deletes a table
     * 
     * @param tableId the table ID
     */
    void deleteTable(Long tableId);

    /**
     * Gets table utilization statistics for a restaurant
     * 
     * @param restaurantId the restaurant ID
     * @return table utilization stats
     */
    TableUtilizationStats getTableUtilization(Long restaurantId);

    /**
     * Command for creating a table
     */
    record CreateTableCommand(
        Long restaurantId,
        Integer seats,
        TableLocation location
    ) {}

    /**
     * Command for updating a table
     */
    record UpdateTableCommand(
        Long id,
        Integer seats,
        TableLocation location
    ) {}

    /**
     * Table utilization statistics
     */
    record TableUtilizationStats(
        Long restaurantId,
        long totalTables,
        long availableTables,
        long unavailableTables,
        int totalSeats,
        double availabilityRate
    ) {}

    /**
     * Exception thrown when table is not found
     */
    class TableNotFoundException extends RuntimeException {
        public TableNotFoundException(Long tableId) {
            super("Table not found with ID: " + tableId);
        }
    }
}