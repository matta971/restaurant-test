package com.restaurant.service.restaurant.infrastructure.adapter.out.persistence;

import com.restaurant.service.restaurant.domain.model.RestaurantTable;
import com.restaurant.service.restaurant.domain.model.TableLocation;
import com.restaurant.service.restaurant.domain.port.out.RestaurantTableRepositoryPort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

/**
 * JPA adapter implementing RestaurantTableRepositoryPort
 * This adapter uses Spring Data JPA to provide persistence operations for restaurant tables
 */
@Repository
public interface RestaurantTableJpaAdapter extends JpaRepository<RestaurantTable, Long> {

    /**
     * Finds all tables for a specific restaurant
     *
     * @param restaurantId the restaurant ID
     * @return list of tables for the restaurant
     */
    @Query("SELECT t FROM RestaurantTable t WHERE t.restaurant.id = :restaurantId ORDER BY t.tableNumber")
    List<RestaurantTable> findByRestaurantId(@Param("restaurantId") Long restaurantId);

    /**
     * Finds available tables for a restaurant
     *
     * @param restaurantId the restaurant ID
     * @return list of available tables
     */
    @Query("SELECT t FROM RestaurantTable t WHERE t.restaurant.id = :restaurantId AND t.available = true ORDER BY t.seats, t.tableNumber")
    List<RestaurantTable> findAvailableByRestaurantId(@Param("restaurantId") Long restaurantId);

    /**
     * Finds tables by location type
     *
     * @param restaurantId the restaurant ID
     * @param location the table location
     * @return list of tables at the specified location
     */
    @Query("SELECT t FROM RestaurantTable t WHERE t.restaurant.id = :restaurantId AND t.location = :location ORDER BY t.seats")
    List<RestaurantTable> findByRestaurantIdAndLocation(@Param("restaurantId") Long restaurantId,
                                                        @Param("location") TableLocation location);

    /**
     * Finds tables by seat count
     *
     * @param restaurantId the restaurant ID
     * @param seats the number of seats
     * @return list of tables with the specified seat count
     */
    @Query("SELECT t FROM RestaurantTable t WHERE t.restaurant.id = :restaurantId AND t.seats = :seats ORDER BY t.tableNumber")
    List<RestaurantTable> findByRestaurantIdAndSeats(@Param("restaurantId") Long restaurantId,
                                                     @Param("seats") Integer seats);

    /**
     * Finds tables that can accommodate a party size
     *
     * @param restaurantId the restaurant ID
     * @param partySize minimum number of seats needed
     * @return list of tables that can accommodate the party
     */
    @Query("SELECT t FROM RestaurantTable t WHERE t.restaurant.id = :restaurantId AND t.seats >= :partySize AND t.available = true ORDER BY t.seats, t.tableNumber")
    List<RestaurantTable> findByRestaurantIdAndSeatsGreaterThanEqual(@Param("restaurantId") Long restaurantId,
                                                                     @Param("partySize") Integer partySize);

    /**
     * Complex query to find available tables for a specific date, time range, and party size
     * This query excludes tables that have overlapping active reservations (AVAILABLE, CONFIRMED status)
     *
     * @param restaurantId the restaurant ID
     * @param date the date
     * @param startTime the start time
     * @param endTime the end time
     * @param partySize minimum seats needed
     * @return list of available tables
     */
    @Query("""
        SELECT t FROM RestaurantTable t 
        WHERE t.restaurant.id = :restaurantId 
        AND t.available = true 
        AND t.seats >= :partySize
        AND t.id NOT IN (
            SELECT DISTINCT ts.table.id FROM TimeSlot ts 
            WHERE ts.table.restaurant.id = :restaurantId
            AND ts.date = :date
            AND ts.status IN ('AVAILABLE', 'CONFIRMED')
            AND ts.startTime < :endTime
            AND ts.endTime > :startTime
        )
        ORDER BY t.seats, t.tableNumber
        """)
    List<RestaurantTable> findAvailableTablesForDateTimeAndPartySize(
            @Param("restaurantId") Long restaurantId,
            @Param("date") LocalDate date,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime,
            @Param("partySize") Integer partySize);

    /**
     * Finds a table by its table number within a restaurant
     *
     * @param restaurantId the restaurant ID
     * @param tableNumber the table number
     * @return optional containing the table, empty if not found
     */
    @Query("SELECT t FROM RestaurantTable t WHERE t.restaurant.id = :restaurantId AND t.tableNumber = :tableNumber")
    Optional<RestaurantTable> findByRestaurantIdAndTableNumber(@Param("restaurantId") Long restaurantId,
                                                               @Param("tableNumber") String tableNumber);

    /**
     * Counts tables for a restaurant
     *
     * @param restaurantId the restaurant ID
     * @return table count
     */
    @Query("SELECT COUNT(t) FROM RestaurantTable t WHERE t.restaurant.id = :restaurantId")
    long countByRestaurantId(@Param("restaurantId") Long restaurantId);

    /**
     * Counts available tables for a restaurant
     *
     * @param restaurantId the restaurant ID
     * @return available table count
     */
    @Query("SELECT COUNT(t) FROM RestaurantTable t WHERE t.restaurant.id = :restaurantId AND t.available = true")
    long countAvailableByRestaurantId(@Param("restaurantId") Long restaurantId);
}