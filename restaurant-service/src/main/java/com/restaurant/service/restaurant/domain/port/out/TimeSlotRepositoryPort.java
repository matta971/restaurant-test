package com.restaurant.service.restaurant.domain.port.out;

import com.restaurant.service.restaurant.domain.model.TimeSlot;
import com.restaurant.service.restaurant.domain.model.TimeSlotStatus;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

/**
 * Port (interface) for TimeSlot persistence operations
 * This is part of the hexagonal architecture - defines the contract
 * that infrastructure adapters must implement
 */
public interface TimeSlotRepositoryPort {

    /**
     * Saves a time slot (create or update)
     *
     * @param timeSlot the time slot to save
     * @return the saved time slot with generated ID if new
     */
    TimeSlot save(TimeSlot timeSlot);

    /**
     * Finds a time slot by its ID
     *
     * @param id the time slot ID
     * @return optional containing the time slot, empty if not found
     */
    Optional<TimeSlot> findById(Long id);

    /**
     * Finds all time slots for a specific table
     *
     * @param tableId the table ID
     * @return list of time slots for the table
     */
    List<TimeSlot> findByTableId(Long tableId);

    /**
     * Finds time slots for a restaurant on a specific date
     *
     * @param restaurantId the restaurant ID
     * @param date the date
     * @return list of time slots for the restaurant on the date
     */
    List<TimeSlot> findByRestaurantIdAndDate(Long restaurantId, LocalDate date);

    /**
     * Finds time slots by status
     *
     * @param restaurantId the restaurant ID
     * @param status the time slot status
     * @return list of time slots with the specified status
     */
    List<TimeSlot> findByRestaurantIdAndStatus(Long restaurantId, TimeSlotStatus status);

    /**
     * Finds time slots in a specific date range
     *
     * @param restaurantId the restaurant ID
     * @param startDate the start date (inclusive)
     * @param endDate the end date (inclusive)
     * @return list of time slots in the date range
     */
    List<TimeSlot> findByRestaurantIdAndDateBetween(Long restaurantId, LocalDate startDate, LocalDate endDate);

    /**
     * Finds available time slots for a restaurant on a specific date
     *
     * @param restaurantId the restaurant ID
     * @param date the date
     * @return list of available time slots
     */
    List<TimeSlot> findAvailableByRestaurantIdAndDate(Long restaurantId, LocalDate date);

    /**
     * Finds conflicting time slots (overlapping with the given time range)
     *
     * @param tableId the table ID
     * @param date the date
     * @param startTime the start time
     * @param endTime the end time
     * @return list of conflicting time slots
     */
    List<TimeSlot> findConflictingTimeSlots(Long tableId, LocalDate date, LocalTime startTime, LocalTime endTime);

    /**
     * Finds upcoming reservations (future date/time from now)
     *
     * @param restaurantId the restaurant ID
     * @param limit maximum number of results
     * @return list of upcoming reservations
     */
    List<TimeSlot> findUpcomingReservations(Long restaurantId, int limit);

    /**
     * Finds reservations for a specific date and time range
     *
     * @param restaurantId the restaurant ID
     * @param date the date
     * @param startTime the start time
     * @param endTime the end time
     * @return list of reservations in the time range
     */
    List<TimeSlot> findReservationsInTimeRange(Long restaurantId, LocalDate date, LocalTime startTime, LocalTime endTime);

    /**
     * Counts reservations by status for a restaurant
     *
     * @param restaurantId the restaurant ID
     * @param status the time slot status
     * @return count of reservations with the specified status
     */
    long countByRestaurantIdAndStatus(Long restaurantId, TimeSlotStatus status);

    /**
     * Counts reservations for a restaurant on a specific date
     *
     * @param restaurantId the restaurant ID
     * @param date the date
     * @return count of reservations on the date
     */
    long countByRestaurantIdAndDate(Long restaurantId, LocalDate date);

    /**
     * Deletes a time slot by ID
     *
     * @param id the time slot ID
     */
    void deleteById(Long id);

    /**
     * Deletes all time slots for a table
     *
     * @param tableId the table ID
     */
    void deleteByTableId(Long tableId);

    /**
     * Checks if a time slot exists with the given ID
     *
     * @param id the time slot ID
     * @return true if exists, false otherwise
     */
    boolean existsById(Long id);

    /**
     * Finds expired time slots that need cleanup
     *
     * @param cutoffDate time slots before this date are considered expired
     * @return list of expired time slots
     */
    List<TimeSlot> findExpiredTimeSlots(LocalDate cutoffDate);

    /**
     * Gets utilization statistics for a restaurant on a specific date
     *
     * @param restaurantId the restaurant ID
     * @param date the date
     * @return utilization data
     */
    UtilizationStats getUtilizationStats(Long restaurantId, LocalDate date);

    /**
     * Data structure for utilization statistics
     */
    record UtilizationStats(
            long totalSlots,
            long reservedSlots,
            long availableSlots,
            long confirmedSlots,
            long cancelledSlots,
            double utilizationRate
    ) {}
}