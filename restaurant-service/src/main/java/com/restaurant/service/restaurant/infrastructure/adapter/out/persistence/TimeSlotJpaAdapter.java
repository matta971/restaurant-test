package com.restaurant.service.restaurant.infrastructure.adapter.out.persistence;

import com.restaurant.service.restaurant.domain.model.TimeSlot;
import com.restaurant.service.restaurant.domain.model.TimeSlotStatus;
import com.restaurant.service.restaurant.domain.port.out.TimeSlotRepositoryPort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * JPA adapter implementing TimeSlotRepositoryPort
 * This adapter provides persistence operations for time slots using Spring Data JPA
 */
@Repository
public interface TimeSlotJpaAdapter extends JpaRepository<TimeSlot, Long> {

    /**
     * Finds all time slots for a specific table
     *
     * @param tableId the table ID
     * @return list of time slots for the table
     */
    @Query("SELECT ts FROM TimeSlot ts WHERE ts.table.id = :tableId ORDER BY ts.date, ts.startTime")
    List<TimeSlot> findByTableId(@Param("tableId") Long tableId);

    /**
     * Finds time slots for a table on a specific date
     *
     * @param tableId the table ID
     * @param date the date
     * @return list of time slots for the date
     */
    @Query("SELECT ts FROM TimeSlot ts WHERE ts.table.id = :tableId AND ts.date = :date ORDER BY ts.startTime")
    List<TimeSlot> findByTableIdAndDate(@Param("tableId") Long tableId, @Param("date") LocalDate date);

    /**
     * Finds time slots by status for a table
     *
     * @param tableId the table ID
     * @param status the status to filter by
     * @return list of time slots with the specified status
     */
    @Query("SELECT ts FROM TimeSlot ts WHERE ts.table.id = :tableId AND ts.status = :status ORDER BY ts.date, ts.startTime")
    List<TimeSlot> findByTableIdAndStatus(@Param("tableId") Long tableId, @Param("status") TimeSlotStatus status);

    /**
     * Finds overlapping time slots for a table on a specific date and time range
     *
     * @param tableId the table ID
     * @param date the date
     * @param startTime the start time
     * @param endTime the end time
     * @return list of overlapping time slots
     */
    @Query("""
        SELECT ts FROM TimeSlot ts 
        WHERE ts.table.id = :tableId 
        AND ts.date = :date
        AND ts.startTime < :endTime 
        AND ts.endTime > :startTime
        ORDER BY ts.startTime
        """)
    List<TimeSlot> findOverlappingTimeSlots(@Param("tableId") Long tableId,
                                            @Param("date") LocalDate date,
                                            @Param("startTime") LocalTime startTime,
                                            @Param("endTime") LocalTime endTime);

    /**
     * Finds active time slots (AVAILABLE, CONFIRMED) for a table in a date range
     *
     * @param tableId the table ID
     * @param startDate the start date (inclusive)
     * @param endDate the end date (inclusive)
     * @return list of active time slots
     */
    @Query("""
        SELECT ts FROM TimeSlot ts 
        WHERE ts.table.id = :tableId 
        AND ts.date BETWEEN :startDate AND :endDate
        AND ts.status IN ('AVAILABLE', 'CONFIRMED')
        ORDER BY ts.date, ts.startTime
        """)
    List<TimeSlot> findActiveTimeSlotsByTableIdAndDateRange(@Param("tableId") Long tableId,
                                                            @Param("startDate") LocalDate startDate,
                                                            @Param("endDate") LocalDate endDate);

    /**
     * Finds time slots for a restaurant on a specific date
     *
     * @param restaurantId the restaurant ID
     * @param date the date
     * @return list of time slots for the restaurant on the date
     */
    @Query("SELECT ts FROM TimeSlot ts WHERE ts.table.restaurant.id = :restaurantId AND ts.date = :date ORDER BY ts.startTime, ts.table.tableNumber")
    List<TimeSlot> findByRestaurantIdAndDate(@Param("restaurantId") Long restaurantId, @Param("date") LocalDate date);

    /**
     * Finds time slots by status for a restaurant
     *
     * @param restaurantId the restaurant ID
     * @param status the status to filter by
     * @return list of time slots with the specified status
     */
    @Query("SELECT ts FROM TimeSlot ts WHERE ts.table.restaurant.id = :restaurantId AND ts.status = :status ORDER BY ts.date, ts.startTime")
    List<TimeSlot> findByRestaurantIdAndStatus(@Param("restaurantId") Long restaurantId, @Param("status") TimeSlotStatus status);

    /**
     * Finds upcoming time slots (today and future) for a restaurant
     *
     * @param restaurantId the restaurant ID
     * @param fromDate the start date (inclusive)
     * @return list of upcoming time slots
     */
    @Query("SELECT ts FROM TimeSlot ts WHERE ts.table.restaurant.id = :restaurantId AND ts.date >= :fromDate ORDER BY ts.date, ts.startTime")
    List<TimeSlot> findUpcomingByRestaurantId(@Param("restaurantId") Long restaurantId, @Param("fromDate") LocalDate fromDate);

    /**
     * Deletes all time slots for a table
     *
     * @param tableId the table ID
     */
    @Modifying
    @Query("DELETE FROM TimeSlot ts WHERE ts.table.id = :tableId")
    void deleteByTableId(@Param("tableId") Long tableId);

    /**
     * Counts time slots for a table
     *
     * @param tableId the table ID
     * @return time slot count
     */
    @Query("SELECT COUNT(ts) FROM TimeSlot ts WHERE ts.table.id = :tableId")
    long countByTableId(@Param("tableId") Long tableId);

    /**
     * Counts time slots by status for a restaurant
     *
     * @param restaurantId the restaurant ID
     * @param status the status to count
     * @return count of time slots with the specified status
     */
    @Query("SELECT COUNT(ts) FROM TimeSlot ts WHERE ts.table.restaurant.id = :restaurantId AND ts.status = :status")
    long countByRestaurantIdAndStatus(@Param("restaurantId") Long restaurantId, @Param("status") TimeSlotStatus status);

    /**
     * Find expired time slots
     */
    @Query("SELECT ts FROM TimeSlot ts WHERE ts.date < :beforeDate")
    List<TimeSlot> findExpiredTimeSlots(@Param("beforeDate") LocalDate beforeDate);

    /**
     * Count time slots for a restaurant on a specific date
     */
    @Query("SELECT COUNT(ts) FROM TimeSlot ts " +
            "JOIN ts.table t " +
            "JOIN t.restaurant r " +
            "WHERE r.id = :restaurantId AND ts.date = :date")
    long countByRestaurantIdAndDate(@Param("restaurantId") Long restaurantId,
                                    @Param("date") LocalDate date);
}