package com.restaurant.service.restaurant.domain.model;

import jakarta.persistence.Embeddable;
import java.util.Objects;

/**
 * Object representing restaurant capacity constraints
 * Encapsulates business rules around seating capacity
 */
@Embeddable
public class RestaurantCapacity {
    
    private static final int MIN_CAPACITY = 1;
    private static final int MAX_CAPACITY = 1000;

    private final int totalSeats;
    private final int maxTableSize;

    // Default constructor for JPA
    protected RestaurantCapacity() {
        this.totalSeats = 0;
        this.maxTableSize = 0;
    }

    /**
     * Creates a restaurant capacity value object
     * 
     * @param totalSeats total number of seats in restaurant
     * @param maxTableSize maximum size for a single table
     * @throws IllegalArgumentException if values are invalid
     */
    public RestaurantCapacity(int totalSeats, int maxTableSize) {
        validateTotalSeats(totalSeats);
        validateMaxTableSize(maxTableSize);
        validateConsistency(totalSeats, maxTableSize);

        this.totalSeats = totalSeats;
        this.maxTableSize = maxTableSize;
    }

    /**
     * Creates a restaurant capacity with default max table size (8)
     * 
     * @param totalSeats total number of seats in restaurant
     * @return new RestaurantCapacity instance
     */
    public static RestaurantCapacity of(int totalSeats) {
        return new RestaurantCapacity(totalSeats, 8);
    }

    /**
     * Creates a restaurant capacity with specified values
     * 
     * @param totalSeats total number of seats in restaurant
     * @param maxTableSize maximum size for a single table
     * @return new RestaurantCapacity instance
     */
    public static RestaurantCapacity of(int totalSeats, int maxTableSize) {
        return new RestaurantCapacity(totalSeats, maxTableSize);
    }

    /**
     * Calculates the utilization rate for given occupied seats
     * 
     * @param occupiedSeats number of currently occupied seats
     * @return utilization rate as percentage (0.0 to 1.0)
     */
    public double calculateUtilizationRate(int occupiedSeats) {
        if (occupiedSeats < 0) {
            throw new IllegalArgumentException("Occupied seats cannot be negative");
        }
        if (occupiedSeats > totalSeats) {
            return 1.0; // Over capacity
        }
        return totalSeats > 0 ? (double) occupiedSeats / totalSeats : 0.0;
    }

    /**
     * Checks if the restaurant can accommodate the requested party size
     * 
     * @param partySize size of the party
     * @return true if can accommodate, false otherwise
     */
    public boolean canAccommodate(int partySize) {
        return partySize > 0 && partySize <= maxTableSize;
    }

    private void validateTotalSeats(int totalSeats) {
        if (totalSeats < MIN_CAPACITY || totalSeats > MAX_CAPACITY) {
            throw new IllegalArgumentException(
                "Total seats must be between " + MIN_CAPACITY + " and " + MAX_CAPACITY);
        }
    }

    private void validateMaxTableSize(int maxTableSize) {
        if (maxTableSize < 1 || maxTableSize > 20) {
            throw new IllegalArgumentException("Max table size must be between 1 and 20");
        }
    }

    private void validateConsistency(int totalSeats, int maxTableSize) {
        if (maxTableSize > totalSeats) {
            throw new IllegalArgumentException("Max table size cannot exceed total capacity");
        }
    }

    // Getters

    public int getTotalSeats() {
        return totalSeats;
    }

    public int getMaxTableSize() {
        return maxTableSize;
    }

    // Value Object equality

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RestaurantCapacity that = (RestaurantCapacity) o;
        return totalSeats == that.totalSeats && maxTableSize == that.maxTableSize;
    }

    @Override
    public int hashCode() {
        return Objects.hash(totalSeats, maxTableSize);
    }

    @Override
    public String toString() {
        return "RestaurantCapacity{" +
                "totalSeats=" + totalSeats +
                ", maxTableSize=" + maxTableSize +
                '}';
    }
}