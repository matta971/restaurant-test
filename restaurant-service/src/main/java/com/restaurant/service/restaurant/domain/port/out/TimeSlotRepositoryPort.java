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
     * Finds time slots for a table on a specific date
     * 
     * @param tableId the table ID
     * @param date the date
     * @return list of time slots for the date
     */
    List<TimeSlot> findByTableIdAndDate(Long tableId, LocalDate date);

    /**
     * Finds time slots by status
     * 
     * @param tableId the table ID
     * @param status the status to filter by
     * @return list of time slots with the specified status
     */
    List<TimeSlot> findByTableIdAndStatus(Long tableId, TimeSlotStatus status);

    /**
     * Finds overlapping time slots for a table on a specific date and time range
     * 
     * @param tableId the table ID
     * @param date the date
     * @param startTime the start time
     * @param endTime the end time
     * @return list of overlapping time slots
     */
    List<TimeSlot> findOverlappingTimeSlots(Long tableId, LocalDate date, LocalTime startTime, LocalTime endTime);

    /**
     * Finds active time slots (AVAILABLE, CONFIRMED) for a table in a date range
     * 
     * @param tableId the table ID
     * @param startDate the start date (inclusive)
     * @param endDate the end date (inclusive)
     * @return list of active time slots
     */
    List<TimeSlot> findActiveTimeSlotsByTableIdAndDateRange(Long tableId, LocalDate startDate, LocalDate endDate);

    /**
     * Finds time slots for a restaurant on a specific date
     * 
     * @param restaurantId the restaurant ID
     * @param date the date
     * @return list of time slots for the restaurant on the date
     */
    List<TimeSlot> findByRestaurantIdAndDate(Long restaurantId, LocalDate date);

    /**
     * Finds time slots by status for a restaurant
     * 
     * @param restaurantId the restaurant ID
     * @param status the status to filter by
     * @return list of time slots with the specified status
     */
    List<TimeSlot> findByRestaurantIdAndStatus(Long restaurantId, TimeSlotStatus status);

    /**
     * Finds upcoming time slots (today and future) for a restaurant
     * 
     * @param restaurantId the restaurant ID
     * @param fromDate the start date (inclusive)
     * @return list of upcoming time slots
     */
    List<TimeSlot> findUpcomingByRestaurantId(Long restaurantId, LocalDate fromDate);

    /**
     * Checks if a time slot exists with the given ID
     * 
     * @param id the time slot ID
     * @return true if exists, false otherwise
     */
    boolean existsById(Long id);

    /**
     * Deletes a time slot by ID
     * 
     * @param id the time slot ID to delete
     */
    void deleteById(Long id);

    /**
     * Deletes all time slots for a table
     * 
     * @param tableId the table ID
     */
    void deleteByTableId(Long tableId);

    /**
     * Counts time slots for a table
     * 
     * @param tableId the table ID
     * @return time slot count
     */
    long countByTableId(Long tableId);

    /**
     * Counts time slots by status for a restaurant
     * 
     * @param restaurantId the restaurant ID
     * @param status the status to count
     * @return count of time slots with the specified status
     */
    long countByRestaurantIdAndStatus(Long restaurantId, TimeSlotStatus status);
}