package com.restaurant.service.restaurant.infrastructure.adapter.out.persistence.repository;

import com.restaurant.service.restaurant.infrastructure.adapter.out.persistence.entity.RestaurantTableEntity;
import com.restaurant.service.restaurant.infrastructure.adapter.out.persistence.entity.RestaurantTableEntity.TableLocationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

/**
 * JPA Repository for RestaurantTable entities
 */
@Repository
public interface RestaurantTableJpaRepository extends JpaRepository<RestaurantTableEntity, Long> {

    /**
     * Find all tables for a restaurant
     */
    List<RestaurantTableEntity> findByRestaurantId(Long restaurantId);

    /**
     * Find available tables for a restaurant
     */
    List<RestaurantTableEntity> findByRestaurantIdAndAvailableTrue(Long restaurantId);

    /**
     * Find tables by location for a restaurant
     */
    List<RestaurantTableEntity> findByRestaurantIdAndLocation(Long restaurantId, TableLocationEntity location);

    /**
     * Find tables that can accommodate party size
     */
    List<RestaurantTableEntity> findByRestaurantIdAndSeatsGreaterThanEqual(Long restaurantId, Integer partySize);

    /**
     * Find available tables that can accommodate party size
     */
    List<RestaurantTableEntity> findByRestaurantIdAndAvailableTrueAndSeatsGreaterThanEqual(
        Long restaurantId, Integer partySize);

    /**
     * Check if table number already exists for restaurant
     */
    boolean existsByRestaurantIdAndTableNumber(Long restaurantId, Integer tableNumber);

    /**
     * Find table by restaurant and table number
     */
    Optional<RestaurantTableEntity> findByRestaurantIdAndTableNumber(Long restaurantId, Integer tableNumber);

    /**
     * Count total tables for restaurant
     */
    long countByRestaurantId(Long restaurantId);

    /**
     * Count available tables for restaurant
     */
    long countByRestaurantIdAndAvailableTrue(Long restaurantId);

    /**
     * Get total seats for restaurant
     */
    @Query("SELECT COALESCE(SUM(t.seats), 0) FROM RestaurantTableEntity t WHERE t.restaurant.id = :restaurantId")
    Integer getTotalSeatsByRestaurantId(@Param("restaurantId") Long restaurantId);

    /**
     * Get available seats for restaurant
     */
    @Query("SELECT COALESCE(SUM(t.seats), 0) FROM RestaurantTableEntity t WHERE t.restaurant.id = :restaurantId AND t.available = true")
    Integer getAvailableSeatsByRestaurantId(@Param("restaurantId") Long restaurantId);

    /**
     * Find available tables for specific date and time range
     */
    @Query("""
        SELECT t FROM RestaurantTableEntity t 
        WHERE t.restaurant.id = :restaurantId 
        AND t.available = true 
        AND t.seats >= :partySize
        AND NOT EXISTS (
            SELECT ts FROM TimeSlotEntity ts 
            WHERE ts.table = t 
            AND ts.date = :date 
            AND ts.status IN ('RESERVED', 'CONFIRMED')
            AND (
                (ts.startTime <= :startTime AND ts.endTime > :startTime) OR
                (ts.startTime < :endTime AND ts.endTime >= :endTime) OR
                (ts.startTime >= :startTime AND ts.endTime <= :endTime)
            )
        )
        ORDER BY t.seats ASC
        """)
    List<RestaurantTableEntity> findAvailableTablesForDateAndTime(
        @Param("restaurantId") Long restaurantId,
        @Param("date") LocalDate date,
        @Param("startTime") LocalTime startTime,
        @Param("endTime") LocalTime endTime,
        @Param("partySize") Integer partySize);

    /**
     * Find table with time slots
     */
    @Query("SELECT t FROM RestaurantTableEntity t LEFT JOIN FETCH t.timeSlots WHERE t.id = :tableId")
    Optional<RestaurantTableEntity> findByIdWithTimeSlots(@Param("tableId") Long tableId);
}