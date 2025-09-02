package com.restaurant.service.restaurant.domain.port.in;

import com.restaurant.service.restaurant.domain.model.RestaurantTable;
import com.restaurant.service.restaurant.domain.model.TimeSlot;
import com.restaurant.service.restaurant.domain.model.TimeSlotStatus;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * Use Case interface for Availability and Reservation management operations
 * This defines the business operations available for checking availability and managing reservations
 */
public interface AvailabilityManagementUseCase {

    /**
     * Finds available tables for a specific date, time, and party size
     * 
     * @param query the availability query
     * @return list of available tables
     */
    List<RestaurantTable> findAvailableTables(AvailabilityQuery query);

    /**
     * Finds the best available table for a party (smallest suitable table)
     * 
     * @param query the availability query
     * @return the best available table, null if none available
     */
    RestaurantTable findBestAvailableTable(AvailabilityQuery query);

    /**
     * Creates a new time slot reservation
     * 
     * @param command the reservation command
     * @return the created time slot
     */
    TimeSlot createReservation(CreateReservationCommand command);

    /**
     * Confirms a reservation
     * 
     * @param timeSlotId the time slot ID to confirm
     * @return the confirmed time slot
     */
    TimeSlot confirmReservation(Long timeSlotId);

    /**
     * Cancels a reservation
     * 
     * @param timeSlotId the time slot ID to cancel
     * @return the cancelled time slot
     */
    TimeSlot cancelReservation(Long timeSlotId);

    /**
     * Completes a reservation (service finished)
     * 
     * @param timeSlotId the time slot ID to complete
     * @return the completed time slot
     */
    TimeSlot completeReservation(Long timeSlotId);

    /**
     * Gets all reservations for a restaurant on a specific date
     * 
     * @param restaurantId the restaurant ID
     * @param date the date
     * @return list of reservations
     */
    List<TimeSlot> getReservationsForDate(Long restaurantId, LocalDate date);

    /**
     * Gets reservations by status for a restaurant
     * 
     * @param restaurantId the restaurant ID
     * @param status the reservation status
     * @return list of reservations with the specified status
     */
    List<TimeSlot> getReservationsByStatus(Long restaurantId, TimeSlotStatus status);

    /**
     * Gets upcoming reservations for a restaurant
     * 
     * @param restaurantId the restaurant ID
     * @return list of upcoming reservations
     */
    List<TimeSlot> getUpcomingReservations(Long restaurantId);

    /**
     * Calculates availability rate for a restaurant on a specific date
     * 
     * @param restaurantId the restaurant ID
     * @param date the date
     * @return availability rate (0.0 to 1.0)
     */
    double calculateAvailabilityRate(Long restaurantId, LocalDate date);

    /**
     * Calculates utilization rate for a restaurant at a specific time
     * 
     * @param restaurantId the restaurant ID
     * @param date the date
     * @param time the time
     * @return utilization rate (0.0 to 1.0)
     */
    double calculateUtilizationRate(Long restaurantId, LocalDate date, LocalTime time);

    /**
     * Gets restaurant capacity statistics
     * 
     * @param restaurantId the restaurant ID
     * @return capacity statistics
     */
    CapacityStats getCapacityStats(Long restaurantId);

    /**
     * Query for checking table availability
     */
    record AvailabilityQuery(
        Long restaurantId,
        LocalDate date,
        LocalTime startTime,
        LocalTime endTime,
        Integer partySize
    ) {}

    /**
     * Command for creating a reservation
     */
    record CreateReservationCommand(
        Long tableId,
        LocalDate date,
        LocalTime startTime,
        LocalTime endTime,
        Integer partySize
    ) {}

    /**
     * Restaurant capacity statistics
     */
    record CapacityStats(
        Long restaurantId,
        int totalSeats,
        int availableSeats,
        long totalTables,
        long availableTables,
        double availabilityRate
    ) {}

    /**
     * Exception thrown when no tables are available
     */
    class NoTablesAvailableException extends RuntimeException {
        public NoTablesAvailableException(Long restaurantId, LocalDate date, LocalTime startTime, LocalTime endTime) {
            super(String.format("No tables available at restaurant %d for %s from %s to %s", 
                restaurantId, date, startTime, endTime));
        }
    }

    /**
     * Exception thrown when time slot is not found
     */
    class TimeSlotNotFoundException extends RuntimeException {
        public TimeSlotNotFoundException(Long timeSlotId) {
            super("Time slot not found with ID: " + timeSlotId);
        }
    }

    /**
     * Exception thrown when reservation operation is not allowed in current state
     */
    class InvalidReservationStateException extends RuntimeException {
        public InvalidReservationStateException(String message) {
            super(message);
        }
    }
}