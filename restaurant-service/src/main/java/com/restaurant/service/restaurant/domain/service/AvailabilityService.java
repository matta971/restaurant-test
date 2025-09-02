package com.restaurant.service.restaurant.domain.service;

import com.restaurant.service.restaurant.domain.model.*;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Domain Service for managing restaurant and table availability
 *
 * This service encapsulates complex business logic that doesn't naturally
 * belong to a single entity. It coordinates between Restaurant, Table, and
 * TimeSlot entities to provide availability management functionality.
 *
 * Responsibilities:
 * - Check table availability across multiple criteria
 * - Find suitable tables for party sizes
 * - Calculate restaurant capacity utilization
 * - Validate reservation constraints
 */
@Service
public class AvailabilityService {

    /**
     * Finds available tables for a given party size, date, and time range
     *
     * @param restaurant the restaurant to search in
     * @param partySize the size of the party
     * @param date the desired date
     * @param startTime the desired start time
     * @param endTime the desired end time
     * @return list of available tables that can accommodate the party
     */
    public List<RestaurantTable> findAvailableTables(Restaurant restaurant, int partySize,
                                                     LocalDate date, LocalTime startTime, LocalTime endTime) {

        validateInputs(restaurant, partySize, date, startTime, endTime);

        return restaurant.getTables().stream()
                .filter(RestaurantTable::isAvailable)
                .filter(table -> table.getSeats() >= partySize)
                .filter(table -> table.isAvailableAt(date, startTime, endTime))
                .collect(Collectors.toList());
    }

    /**
     * Finds the best table for a party size (smallest table that fits)
     *
     * @param restaurant the restaurant to search in
     * @param partySize the size of the party
     * @param date the desired date
     * @param startTime the desired start time
     * @param endTime the desired end time
     * @return optional containing the best table, empty if none available
     */
    public Optional<RestaurantTable> findBestTable(Restaurant restaurant, int partySize,
                                                   LocalDate date, LocalTime startTime, LocalTime endTime) {

        List<RestaurantTable> availableTables = findAvailableTables(restaurant, partySize, date, startTime, endTime);

        return availableTables.stream()
                .min((t1, t2) -> Integer.compare(t1.getSeats(), t2.getSeats()));
    }

    /**
     * Calculates the availability rate for a restaurant on a given date
     *
     * @param restaurant the restaurant
     * @param date the date to check
     * @return availability rate as percentage (0.0 to 1.0)
     */
    public double calculateAvailabilityRate(Restaurant restaurant, LocalDate date) {
        validateRestaurant(restaurant);

        List<RestaurantTable> allTables = restaurant.getTables();
        if (allTables.isEmpty()) {
            return 0.0;
        }

        long availableTablesCount = allTables.stream()
                .filter(RestaurantTable::isAvailable)
                .count();

        return (double) availableTablesCount / allTables.size();
    }

    /**
     * Calculates the utilization rate for a restaurant at a specific time
     *
     * @param restaurant the restaurant
     * @param date the date
     * @param time the specific time
     * @return utilization rate as percentage (0.0 to 1.0)
     */
    public double calculateUtilizationRate(Restaurant restaurant, LocalDate date, LocalTime time) {
        validateRestaurant(restaurant);

        int totalSeats = restaurant.getTotalAvailableSeats();
        if (totalSeats == 0) {
            return 0.0;
        }

        int occupiedSeats = restaurant.getTables().stream()
                .filter(RestaurantTable::isAvailable)
                .mapToInt(table -> getOccupiedSeatsAt(table, date, time))
                .sum();

        return (double) occupiedSeats / totalSeats;
    }

    /**
     * Checks if a restaurant can accommodate a party size at any time on a given date
     *
     * @param restaurant the restaurant
     * @param partySize the party size
     * @param date the date
     * @return true if can accommodate at some point, false otherwise
     */
    public boolean canAccommodateOnDate(Restaurant restaurant, int partySize, LocalDate date) {
        validateRestaurant(restaurant);

        return restaurant.getTables().stream()
                .filter(RestaurantTable::isAvailable)
                .anyMatch(table -> table.getSeats() >= partySize);
    }

    /**
     * Gets all available time slots for a table on a specific date
     *
     * @param table the table
     * @param date the date
     * @param intervalMinutes interval between slots in minutes (e.g., 30, 60)
     * @return list of available time ranges
     */
    public List<TimeRange> getAvailableTimeSlots(RestaurantTable table, LocalDate date, int intervalMinutes) {
        validateTable(table);

        // This is a simplified implementation
        // In a real system, you'd generate time slots based on restaurant opening hours
        // and subtract existing reservations

        LocalTime startTime = LocalTime.of(11, 0); // Opening time
        LocalTime endTime = LocalTime.of(23, 0);   // Last seating

        List<TimeRange> availableSlots = new java.util.ArrayList<>();

        LocalTime current = startTime;
        while (current.plusMinutes(intervalMinutes).isBefore(endTime) ||
                current.plusMinutes(intervalMinutes).equals(endTime)) {

            LocalTime slotEnd = current.plusMinutes(intervalMinutes);
            if (table.isAvailableAt(date, current, slotEnd)) {
                availableSlots.add(new TimeRange(current, slotEnd));
            }
            current = current.plusMinutes(intervalMinutes);
        }

        return availableSlots;
    }

    /**
     * Validates if a reservation request meets business constraints
     *
     * @param restaurant the restaurant
     * @param table the table
     * @param partySize the party size
     * @param date the date
     * @param startTime the start time
     * @param endTime the end time
     * @throws IllegalArgumentException if constraints are not met
     */
    public void validateReservationConstraints(Restaurant restaurant, RestaurantTable table, int partySize,
                                               LocalDate date, LocalTime startTime, LocalTime endTime) {

        validateInputs(restaurant, partySize, date, startTime, endTime);
        validateTable(table);

        if (!restaurant.isActive()) {
            throw new IllegalArgumentException("Restaurant is not active");
        }

        if (!table.isAvailable()) {
            throw new IllegalArgumentException("Table is not available");
        }

        if (table.getSeats() < partySize) {
            throw new IllegalArgumentException("Table capacity insufficient for party size");
        }

        if (!table.isAvailableAt(date, startTime, endTime)) {
            throw new IllegalArgumentException("Table is not available at requested time");
        }

        // Check if table location requires special handling
        if (table.getLocation().requiresReservation() && partySize < 4) {
            throw new IllegalArgumentException("Private room requires minimum 4 guests");
        }

        if (table.getLocation().isWeatherDependent()) {
            // In a real system, you might check weather forecast
            // For now, just add a warning in logs
            System.out.println("Warning: Terrace table booking - weather dependent");
        }
    }

    // Helper methods

    private int getOccupiedSeatsAt(RestaurantTable table, LocalDate date, LocalTime time) {
        return table.getTimeSlots().stream()
                .filter(slot -> slot.getStatus() == TimeSlotStatus.CONFIRMED ||
                        slot.getStatus() == TimeSlotStatus.AVAILABLE) // Both CONFIRMED and AVAILABLE count as occupied
                .filter(slot -> slot.getDate().equals(date))
                .filter(slot -> slot.getStartTime().isBefore(time) || slot.getStartTime().equals(time))
                .filter(slot -> slot.getEndTime().isAfter(time))
                .mapToInt(TimeSlot::getReservedSeats)
                .sum();
    }

    private void validateInputs(Restaurant restaurant, int partySize, LocalDate date,
                                LocalTime startTime, LocalTime endTime) {
        validateRestaurant(restaurant);

        if (partySize <= 0) {
            throw new IllegalArgumentException("Party size must be positive");
        }

        if (date == null) {
            throw new IllegalArgumentException("Date cannot be null");
        }

        if (date.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Cannot book for past dates");
        }

        if (startTime == null || endTime == null) {
            throw new IllegalArgumentException("Start and end times cannot be null");
        }

        if (!endTime.isAfter(startTime)) {
            throw new IllegalArgumentException("End time must be after start time");
        }
    }

    private void validateRestaurant(Restaurant restaurant) {
        if (restaurant == null) {
            throw new IllegalArgumentException("Restaurant cannot be null");
        }
    }

    private void validateTable(RestaurantTable table) {
        if (table == null) {
            throw new IllegalArgumentException("Table cannot be null");
        }
    }
}