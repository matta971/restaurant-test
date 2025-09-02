package com.restaurant.service.restaurant.infrastructure.adapter.out.persistence.repository;

import com.restaurant.service.restaurant.infrastructure.adapter.out.persistence.entity.TimeSlotEntity;
import com.restaurant.service.restaurant.infrastructure.adapter.out.persistence.entity.TimeSlotEntity.TimeSlotStatusEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

/**
 * JPA Repository for TimeSlot entities
 */
@Repository
public interface TimeSlotJpaRepository extends JpaRepository<TimeSlotEntity, Long> {

    /**
     * Find time slots for a table on a specific date
     */
    List<TimeSlotEntity> findByTableIdAndDate(Long tableId, LocalDate date);

    /**
     * Find time slots by status for a table on a date
     */
    List<TimeSlotEntity> findByTableIdAndDateAndStatus(Long tableId, LocalDate date, TimeSlotStatusEntity status);

    /**
     * Find available time slots for a table on a date
     */
    List<TimeSlotEntity> findByTableIdAndDateAndStatusOrderByStartTime(
        Long tableId, LocalDate date, TimeSlotStatusEntity status);

    /**
     * Find time slots for a restaurant on a date
     */
    @Query("""
        SELECT ts FROM TimeSlotEntity ts 
        JOIN ts.table t 
        WHERE t.restaurant.id = :restaurantId 
        AND ts.date = :date
        ORDER BY ts.startTime
        """)
    List<TimeSlotEntity> findByRestaurantAndDate(@Param("restaurantId") Long restaurantId, @Param("date") LocalDate date);

    /**
     * Find time slots by customer email
     */
    List<TimeSlotEntity> findByCustomerEmailOrderByDateDescStartTimeDesc(String customerEmail);

    /**
     * Find conflicting time slots for a table
     */
    @Query("""
        SELECT ts FROM TimeSlotEntity ts 
        WHERE ts.table.id = :tableId 
        AND ts.date = :date 
        AND ts.status IN ('RESERVED', 'CONFIRMED')
        AND (
            (ts.startTime <= :startTime AND ts.endTime > :startTime) OR
            (ts.startTime < :endTime AND ts.endTime >= :endTime) OR
            (ts.startTime >= :startTime AND ts.endTime <= :endTime)
        )
        """)
    List<TimeSlotEntity> findConflictingTimeSlots(
        @Param("tableId") Long tableId,
        @Param("date") LocalDate date,
        @Param("startTime") LocalTime startTime,
        @Param("endTime") LocalTime endTime);

    /**
     * Check if time slot exists for table at specific time
     */
    boolean existsByTableIdAndDateAndStartTimeAndEndTime(
        Long tableId, LocalDate date, LocalTime startTime, LocalTime endTime);

    /**
     * Find upcoming reservations for a restaurant
     */
    @Query("""
        SELECT ts FROM TimeSlotEntity ts 
        JOIN ts.table t 
        WHERE t.restaurant.id = :restaurantId 
        AND ts.date >= :fromDate 
        AND ts.status IN ('RESERVED', 'CONFIRMED')
        ORDER BY ts.date, ts.startTime
        """)
    List<TimeSlotEntity> findUpcomingReservations(
        @Param("restaurantId") Long restaurantId, @Param("fromDate") LocalDate fromDate);

    /**
     * Find reservations for date range
     */
    @Query("""
        SELECT ts FROM TimeSlotEntity ts 
        JOIN ts.table t 
        WHERE t.restaurant.id = :restaurantId 
        AND ts.date BETWEEN :fromDate AND :toDate
        ORDER BY ts.date, ts.startTime
        """)
    List<TimeSlotEntity> findReservationsByDateRange(
        @Param("restaurantId") Long restaurantId,
        @Param("fromDate") LocalDate fromDate,
        @Param("toDate") LocalDate toDate);

    /**
     * Count reservations by status for a restaurant
     */
    @Query("""
        SELECT COUNT(ts) FROM TimeSlotEntity ts 
        JOIN ts.table t 
        WHERE t.restaurant.id = :restaurantId 
        AND ts.status = :status
        """)
    long countByRestaurantAndStatus(@Param("restaurantId") Long restaurantId, @Param("status") TimeSlotStatusEntity status);

    /**
     * Find time slot with table details
     */
    @Query("SELECT ts FROM TimeSlotEntity ts JOIN FETCH ts.table t JOIN FETCH t.restaurant WHERE ts.id = :timeSlotId")
    Optional<TimeSlotEntity> findByIdWithDetails(@Param("timeSlotId") Long timeSlotId);

    /**
     * Delete old time slots (cleanup)
     */
    void deleteByDateBefore(LocalDate cutoffDate);
}